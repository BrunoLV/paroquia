package br.com.nsfatima.gestao.importacao.application.service;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CriarEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.ResultadoOperacaoEvento;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.CriarEventoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.importacao.domain.model.DesfechoLinha;
import br.com.nsfatima.gestao.importacao.domain.model.TipoEntidadeImportacao;
import br.com.nsfatima.gestao.organizacao.application.service.OrganizacaoService;
import br.com.nsfatima.gestao.organizacao.application.service.ValidadorHierarquiaOrganizacional;
import br.com.nsfatima.gestao.organizacao.application.usecase.AdicionarVinculoUseCase;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.policy.PapeisPorTipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Interpreta e grava uma linha de importação por meio das regras do contexto de destino, como se o usuário a
 * tivesse cadastrado pela tela (RN-IMP-02).
 *
 * <p>A validação (dry-run) e a gravação compartilham o mesmo {@code preparar}: o texto da planilha vira um
 * comando tipado, sem nenhum valor assumido por omissão — campo ausente, UUID malformado, enum desconhecido ou
 * data ilegível rejeitam a linha com mensagem clara. Quem decide o que é permitido são os casos de uso de
 * destino (vínculo de organização, serviço de organização e criação de evento do calendário); este componente
 * apenas os chama, de modo que o "válida" do dry-run não possa divergir da confirmação.</p>
 *
 * <p>Cada gravação roda em transação própria ({@link Propagation#REQUIRES_NEW}) e deixa a exceção subir,
 * de modo que a falha de uma linha desfaz somente essa linha e quem confirma o lote possa relatá-la sem
 * perder as demais.</p>
 */
@Component
public class ImportacaoLinhaExecutor {

    private static final String CAMPO_NOME = "nome";
    private static final String CAMPO_IGREJA = "igrejaId";
    private static final ZoneId FUSO_PAROQUIA = ZoneId.of("America/Sao_Paulo");
    private static final Pattern FORMATO_UUID =
            Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Papéis de vínculo que a importação pode atribuir. Coordenação, vice-coordenação (secretário de grupo) e
     * papéis de conselho dependem de mandato, idoneidade e provisão canônica do Pároco (RN-ORG-05), então só
     * existem pela tela.
     */
    private static final Set<PapelOrganizacional> PAPEIS_IMPORTAVEIS =
            EnumSet.of(PapelOrganizacional.MEMBRO_ATIVO, PapelOrganizacional.VOLUNTARIO);

    private sealed interface Comando permits ComandoMembro, ComandoOrganizacao, ComandoEvento {
    }

    private record ComandoMembro(UUID usuarioId, UUID organizacaoId, TipoOrganizacao tipo,
                                 PapelOrganizacional papel, UUID igrejaId) implements Comando {
    }

    private record ComandoOrganizacao(String nome, TipoOrganizacao tipo, String contato, UUID parentId,
                                      UUID pastoralOficialId, SetorPastoral setorPastoral,
                                      SubtipoConselho subtipoConselho) implements Comando {
    }

    private record ComandoEvento(CriarEventoRequest request, String chaveIdempotencia) implements Comando {
    }

    private final UsuarioJpaRepository usuarioJpaRepository;
    private final OrganizacaoJpaRepository organizacaoJpaRepository;
    private final UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository;
    private final AdicionarVinculoUseCase adicionarVinculoUseCase;
    private final OrganizacaoService organizacaoService;
    private final ValidadorHierarquiaOrganizacional hierarquiaOrganizacional;
    private final CriarEventoUseCase criarEventoUseCase;
    private final Validator validator;

    public ImportacaoLinhaExecutor(UsuarioJpaRepository usuarioJpaRepository,
                                   OrganizacaoJpaRepository organizacaoJpaRepository,
                                   UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository,
                                   AdicionarVinculoUseCase adicionarVinculoUseCase,
                                   OrganizacaoService organizacaoService,
                                   ValidadorHierarquiaOrganizacional hierarquiaOrganizacional,
                                   CriarEventoUseCase criarEventoUseCase,
                                   Validator validator) {
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.organizacaoJpaRepository = organizacaoJpaRepository;
        this.unidadeTerritorialJpaRepository = unidadeTerritorialJpaRepository;
        this.adicionarVinculoUseCase = adicionarVinculoUseCase;
        this.organizacaoService = organizacaoService;
        this.hierarquiaOrganizacional = hierarquiaOrganizacional;
        this.criarEventoUseCase = criarEventoUseCase;
        this.validator = validator;
    }

    /**
     * Dry-run: interpreta a linha e aplica as validações de domínio do destino sem persistir nada.
     *
     * @param tipo tipo de entidade da linha
     * @param dados valores da linha por nome de coluna
     * @throws RuntimeException com a regra violada quando a linha não pode ser importada
     */
    public void validar(TipoEntidadeImportacao tipo, Map<String, String> dados) {
        // O vínculo de membro é conferido por inteiro em preparar; organização e evento têm ainda a política
        // de destino, que só o caso de uso conhece.
        Comando comando = preparar(tipo, dados);
        if (comando instanceof ComandoOrganizacao o) {
            hierarquiaOrganizacional.validar(null, o.tipo(), o.parentId(), o.pastoralOficialId(),
                    o.setorPastoral(), o.subtipoConselho());
        } else if (comando instanceof ComandoEvento e) {
            criarEventoUseCase.validarCriacao(e.request());
        }
    }

    /**
     * Grava a linha em transação própria, pelo caso de uso de destino. Qualquer falha desfaz apenas esta linha e
     * é relançada a quem confirma o lote.
     *
     * @param tipo tipo de entidade da linha
     * @param dados valores da linha por nome de coluna
     * @return se a linha foi gravada ou ficou aguardando aprovação
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DesfechoLinha processar(TipoEntidadeImportacao tipo, Map<String, String> dados) {
        return switch (preparar(tipo, dados)) {
            case ComandoMembro m -> {
                adicionarVinculoUseCase.execute(m.usuarioId(), m.organizacaoId(), m.tipo(), m.papel(), m.igrejaId());
                yield DesfechoLinha.IMPORTADA;
            }
            case ComandoOrganizacao o -> {
                organizacaoService.criar(o.nome(), o.tipo(), o.contato(), o.parentId(), o.pastoralOficialId(),
                        o.setorPastoral(), o.subtipoConselho());
                yield DesfechoLinha.IMPORTADA;
            }
            case ComandoEvento e -> criarEventoUseCase.execute(e.chaveIdempotencia(), e.request())
                    instanceof ResultadoOperacaoEvento.Pendente
                    ? DesfechoLinha.AGUARDANDO_APROVACAO : DesfechoLinha.IMPORTADA;
        };
    }

    private Comando preparar(TipoEntidadeImportacao tipo, Map<String, String> dados) {
        return switch (tipo) {
            case MEMBRO -> prepararMembro(dados);
            case ORGANIZACAO -> prepararOrganizacao(dados);
            case EVENTO -> prepararEvento(dados);
        };
    }

    private ComandoMembro prepararMembro(Map<String, String> dados) {
        String username = obrigatorio(dados, "usuarioUsername");
        String organizacaoNome = obrigatorio(dados, "organizacaoNome");
        String papelBruto = obrigatorio(dados, "papel");

        var usuario = usuarioJpaRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + username));
        if (usuario.getFielId() == null) {
            throw new IllegalArgumentException("Usuário sem fiel vinculado: " + username);
        }
        var organizacao = organizacaoJpaRepository.findByNomeIgnoreCase(organizacaoNome)
                .orElseThrow(() -> new IllegalArgumentException("Organização não encontrada: " + organizacaoNome));

        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(papelBruto);
        if (papel == null) {
            throw new IllegalArgumentException("Valor inválido para 'papel'. Valores aceitos: "
                    + nomes(PAPEIS_IMPORTAVEIS) + ".");
        }
        if (!PAPEIS_IMPORTAVEIS.contains(papel)) {
            throw new IllegalArgumentException("A importação só atribui os papéis MEMBRO_ATIVO e VOLUNTARIO; "
                    + "coordenação, vice-coordenação e papéis de conselho mudam pela tela, por mandato (RN-ORG-05).");
        }
        PapeisPorTipoOrganizacao.exigir(organizacao.getTipo(), papel);

        UUID igrejaId = uuid(CAMPO_IGREJA, texto(dados, CAMPO_IGREJA));
        boolean exigeIgreja = organizacao.getTipo() == TipoOrganizacao.PASTORAL
                || organizacao.getTipo() == TipoOrganizacao.MOVIMENTO;
        if (igrejaId == null && exigeIgreja) {
            throw campoInvalido(CAMPO_IGREJA, "é obrigatório para pastorais e movimentos.");
        }
        exigirIgrejaExistente(igrejaId);
        return new ComandoMembro(usuario.getId(), organizacao.getId(), organizacao.getTipo(), papel, igrejaId);
    }

    private ComandoOrganizacao prepararOrganizacao(Map<String, String> dados) {
        String nome = obrigatorio(dados, CAMPO_NOME);
        TipoOrganizacao tipo = enumeracao(TipoOrganizacao.class, "tipo", obrigatorio(dados, "tipo"));
        return new ComandoOrganizacao(nome, tipo, vazioParaNulo(texto(dados, "contato")),
                uuid("parentId", texto(dados, "parentId")),
                uuid("pastoralOficialId", texto(dados, "pastoralOficialId")),
                enumeracao(SetorPastoral.class, "setorPastoral", texto(dados, "setorPastoral")),
                enumeracao(SubtipoConselho.class, "subtipoConselho", texto(dados, "subtipoConselho")));
    }

    private ComandoEvento prepararEvento(Map<String, String> dados) {
        String nome = obrigatorio(dados, CAMPO_NOME);
        Instant inicio = instante("dataInicio", obrigatorio(dados, "dataInicio"));
        Instant fim = instante("dataFim", obrigatorio(dados, "dataFim"));
        UUID organizacaoId = uuid("organizacaoId", obrigatorio(dados, "organizacaoId"));
        if (!organizacaoJpaRepository.existsById(organizacaoId)) {
            throw new IllegalArgumentException("Organização responsável não encontrada.");
        }
        CategoriaEvento categoria = enumeracao(CategoriaEvento.class, "categoria", obrigatorio(dados, "categoria"));
        UUID igrejaId = uuid(CAMPO_IGREJA, texto(dados, CAMPO_IGREJA));
        exigirIgrejaExistente(igrejaId);

        CriarEventoRequest request = new CriarEventoRequest(nome, vazioParaNulo(texto(dados, "descricao")),
                categoria, organizacaoId, null, uuid("localId", texto(dados, "localId")), null, igrejaId,
                inicio, fim, null, vazioParaNulo(texto(dados, "justificativa")), null, false, null, null, false,
                null, null, null);
        var violacoes = validator.validate(request);
        if (!violacoes.isEmpty()) {
            throw new IllegalArgumentException(violacoes.stream().map(ConstraintViolation::getMessage)
                    .sorted().collect(Collectors.joining(" ")));
        }
        return new ComandoEvento(request, "importacao-"
                + UUID.nameUUIDFromBytes(request.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private void exigirIgrejaExistente(UUID igrejaId) {
        if (igrejaId != null && unidadeTerritorialJpaRepository.findById(igrejaId).isEmpty()) {
            throw new IllegalArgumentException("Igreja de vínculo não encontrada.");
        }
    }

    private static String texto(Map<String, String> dados, String campo) {
        String valor = dados.get(campo);
        return valor == null ? "" : valor.trim();
    }

    private static String obrigatorio(Map<String, String> dados, String campo) {
        String valor = texto(dados, campo);
        if (valor.isEmpty()) {
            throw campoInvalido(campo, "é obrigatório.");
        }
        return valor;
    }

    private static IllegalArgumentException campoInvalido(String campo, String problema) {
        return new IllegalArgumentException("O campo '" + campo + "' " + problema);
    }

    private static String vazioParaNulo(String valor) {
        return valor.isEmpty() ? null : valor;
    }

    private static UUID uuid(String campo, String valor) {
        if (valor.isEmpty()) {
            return null;
        }
        if (!FORMATO_UUID.matcher(valor).matches()) {
            throw campoInvalido(campo, "não é um identificador válido.");
        }
        return UUID.fromString(valor);
    }

    private static <E extends Enum<E>> E enumeracao(Class<E> tipo, String campo, String valor) {
        if (valor.isEmpty()) {
            return null;
        }
        return Arrays.stream(tipo.getEnumConstants()).filter(c -> c.name().equalsIgnoreCase(valor)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Valor inválido para '" + campo
                        + "'. Valores aceitos: " + nomes(Arrays.asList(tipo.getEnumConstants())) + "."));
    }

    private static String nomes(Collection<? extends Enum<?>> constantes) {
        return constantes.stream().map(Enum::name).collect(Collectors.joining(", "));
    }

    private static Instant instante(String campo, String valor) {
        try {
            return Instant.parse(valor);
        } catch (DateTimeParseException ignorado) {
            // tenta os formatos locais abaixo
        }
        try {
            return LocalDateTime.parse(valor, DATA_HORA).atZone(FUSO_PAROQUIA).toInstant();
        } catch (DateTimeParseException ignorado) {
            // tenta o formato que a leitura de XLSX produz para células de data
        }
        try {
            return LocalDateTime.parse(valor).atZone(FUSO_PAROQUIA).toInstant();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("O campo '" + campo
                    + "' deve estar no formato 'yyyy-MM-dd HH:mm' (horário de Brasília) ou ISO-8601 em UTC.");
        }
    }
}
