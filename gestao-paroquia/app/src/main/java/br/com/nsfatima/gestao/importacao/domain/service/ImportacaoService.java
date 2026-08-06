package br.com.nsfatima.gestao.importacao.domain.service;

import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoItemResultadoDTO;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoValidacaoResultadoDTO;
import br.com.nsfatima.gestao.importacao.domain.model.TipoEntidadeImportacao;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.application.usecase.AdicionarMembroUseCase;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.domain.service.OrganizacaoService;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.service.MotorLiturgico;
import br.com.nsfatima.gestao.calendario.domain.service.ProvisionamentoLiturgicoService;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestra o ciclo completo de importação em lote (template → validação → confirmação) para
 * os quatro tipos suportados por {@link TipoEntidadeImportacao}: membro, usuário, organização e
 * evento. A validação (dry-run) nunca persiste nada — apenas relata, linha a linha, se o dado
 * é aceitável — e a confirmação processa cada linha de forma independente (falha em uma linha
 * não interrompe as demais; é apenas logada e descontada da contagem de sucesso), pois o lote
 * inteiro não roda em uma única transação atômica.
 */
@Service
public class ImportacaoService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoService.class);
    private static final String KEY_NOME = "nome";
    private static final String ERR_NOME_REQUIRED = "O campo 'nome' é obrigatório.";
    private static final String KEY_IGREJA_ID = "igrejaId";

    private final AdicionarMembroUseCase adicionarMembroUseCase;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final OrganizacaoService organizacaoService;
    private final EventoJpaRepository eventoJpaRepository;
    private final OrganizacaoJpaRepository organizacaoJpaRepository;
    private final PastoralMovimentoOficialRepository pastoralOficialRepository;
    private final ImportacaoFileParser importacaoFileParser;
    private final AnoParoquialJpaRepository anoParoquialJpaRepository;
    private final MotorLiturgico liturgicalEngine;
    private final CicloLiturgicoRepository cicloLiturgicoRepository;
    private final ProvisionamentoLiturgicoService liturgicalProvisioningService;

    public ImportacaoService(AdicionarMembroUseCase adicionarMembroUseCase,
                              UsuarioJpaRepository usuarioJpaRepository,
                              OrganizacaoService organizacaoService,
                              EventoJpaRepository eventoJpaRepository,
                              OrganizacaoJpaRepository organizacaoJpaRepository,
                              PastoralMovimentoOficialRepository pastoralOficialRepository,
                              ImportacaoFileParser importacaoFileParser,
                              AnoParoquialJpaRepository anoParoquialJpaRepository,
                              MotorLiturgico liturgicalEngine,
                              CicloLiturgicoRepository cicloLiturgicoRepository,
                              ProvisionamentoLiturgicoService liturgicalProvisioningService) {
        this.adicionarMembroUseCase = adicionarMembroUseCase;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.organizacaoService = organizacaoService;
        this.eventoJpaRepository = eventoJpaRepository;
        this.organizacaoJpaRepository = organizacaoJpaRepository;
        this.pastoralOficialRepository = pastoralOficialRepository;
        this.importacaoFileParser = importacaoFileParser;
        this.anoParoquialJpaRepository = anoParoquialJpaRepository;
        this.liturgicalEngine = liturgicalEngine;
        this.cicloLiturgicoRepository = cicloLiturgicoRepository;
        this.liturgicalProvisioningService = liturgicalProvisioningService;
    }

    /**
     * Gera o arquivo modelo (CSV ou XLSX) que serve de guia de preenchimento para o tipo de
     * entidade informado, delegando ao parser o formato/colunas específicos de cada tipo.
     *
     * @param tipo código de {@link TipoEntidadeImportacao}
     * @param formato "CSV" ou "XLSX"
     * @return bytes do arquivo gerado
     * @throws IOException se houver falha ao montar a planilha/arquivo
     */
    public byte[] gerarTemplate(String tipo, String formato) throws IOException {
        return importacaoFileParser.gerarTemplate(tipo, formato);
    }

    /**
     * Executa a etapa de pré-validação (dry-run): faz o parse do arquivo enviado e valida cada
     * linha isoladamente contra as regras do tipo de entidade, sem persistir nada. Cada linha é
     * avaliada de forma independente — um erro em uma linha não invalida as demais — para que o
     * usuário veja de uma só vez todos os problemas do arquivo antes de corrigir e confirmar.
     * O número de linha reportado ({@code linhaNum = i + 2}) já soma o deslocamento do
     * cabeçalho, para bater com o que o usuário vê ao abrir o arquivo original.
     *
     * @param tipo código de {@link TipoEntidadeImportacao}
     * @param file arquivo CSV ou XLSX enviado pelo usuário
     * @return sumário com totais e detalhe de validação por linha
     * @throws IOException se o arquivo não puder ser lido/parseado
     */
    public ImportacaoValidacaoResultadoDTO validarArquivo(String tipo, MultipartFile file) throws IOException {
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
        List<Map<String, String>> rows = importacaoFileParser.parseFile(filename, file.getInputStream());

        List<ImportacaoItemResultadoDTO> itens = new ArrayList<>();
        int valids = 0;
        int invalidos = 0;

        for (int i = 0; i < rows.size(); i++) {
            int linhaNum = i + 2;
            Map<String, String> rowData = rows.get(i);
            String errorMsg = validarLinha(tipo, rowData);
            boolean isValido = (errorMsg == null);

            if (isValido) {
                valids++;
            } else {
                invalidos++;
            }
            itens.add(new ImportacaoItemResultadoDTO(linhaNum, isValido, errorMsg, rowData));
        }

        return new ImportacaoValidacaoResultadoDTO(rows.size(), valids, invalidos, itens);
    }

    /**
     * Despacha a validação de uma linha para a regra específica do tipo de entidade. Retorna
     * {@code null} quando a linha é válida — convenção usada em todo o fluxo de validação em vez
     * de lançar exceção, pois um erro de linha não deve interromper a validação do restante do
     * arquivo.
     *
     * @return mensagem de erro amigável ao usuário, ou {@code null} se a linha é válida
     */
    private String validarLinha(String tipo, Map<String, String> data) {
        TipoEntidadeImportacao tipoEntidade = TipoEntidadeImportacao.fromCode(tipo);
        if (tipoEntidade == null) {
            return "Tipo de entidade desconhecido";
        }
        return switch (tipoEntidade) {
            case MEMBRO -> validarMembro(data);
            case ORGANIZACAO -> validarOrganizacao(data);
            case EVENTO -> validarEvento(data);
        };
    }

    /**
     * Valida uma linha de importação de MEMBRO: exige que o usuário ("usuarioUsername") e a
     * organização ("organizacaoNome") já existam previamente no sistema — importação de membro
     * apenas cria o vínculo, nunca as entidades relacionadas — e que um "papel" tenha sido
     * informado.
     */
    private String validarMembro(Map<String, String> data) {
        String username = data.getOrDefault("usuarioUsername", "").trim();
        if (username.isBlank()) {
            return "O campo 'usuarioUsername' é obrigatório.";
        }
        var usuarioOpt = usuarioJpaRepository.findByUsernameIgnoreCase(username);
        if (usuarioOpt.isEmpty()) {
            return "Usuário não encontrado: " + username;
        }

        String orgNome = data.getOrDefault("organizacaoNome", "").trim();
        if (orgNome.isBlank()) {
            return "O campo 'organizacaoNome' é obrigatório.";
        }
        var orgOpt = organizacaoJpaRepository.findByNomeIgnoreCase(orgNome);
        if (orgOpt.isEmpty()) {
            return "Organização não encontrada: " + orgNome;
        }

        if (data.getOrDefault("papel", "").isBlank()) {
            return "O campo 'papel' é obrigatório.";
        }
        return null;
    }

    /**
     * Valida uma linha de importação de ORGANIZACAO: exige nome e tipo; o "tipo" precisa ser um
     * valor de {@code TipoOrganizacao} válido (checado apenas na importação, via
     * {@link #parseTipoOrganizacao}, que aplica um fallback silencioso se o valor for inválido).
     */
    private String validarOrganizacao(Map<String, String> data) {
        if (data.getOrDefault(KEY_NOME, "").isBlank()) {
            return ERR_NOME_REQUIRED;
        }
        if (data.getOrDefault("tipo", "").isBlank()) {
            return "O campo 'tipo' é obrigatório.";
        }
        return null;
    }

    /**
     * Valida uma linha de importação de EVENTO: exige nome e data de início; demais campos
     * (categoria, local, organização responsável) são resolvidos com fallback na importação e
     * por isso não são obrigatórios na validação.
     */
    private String validarEvento(Map<String, String> data) {
        if (data.getOrDefault(KEY_NOME, "").isBlank()) {
            return ERR_NOME_REQUIRED;
        }
        if (data.getOrDefault("dataInicio", "").isBlank()) {
            return "O campo 'dataInicio' é obrigatório.";
        }
        return null;
    }

    /**
     * Persiste os itens já validados pelo cliente (tipicamente após {@link #validarArquivo}),
     * criando um registro por linha. Cada linha é processada e persistida individualmente,
     * dentro da mesma transação: uma exceção em uma linha é capturada e apenas logada, sem
     * interromper as demais e sem provocar rollback do lote inteiro — importação em lote
     * prioriza "importar o máximo possível" sobre atomicidade estrita.
     *
     * @param tipo código de {@link TipoEntidadeImportacao}
     * @param validItems linhas já consideradas válidas pelo chamador (dados brutos por coluna)
     * @param actor identificador do usuário autenticado que disparou a importação, usado como
     *     autor do vínculo criado em {@link #importMembro}
     * @return quantidade de linhas efetivamente persistidas com sucesso
     * @throws IllegalArgumentException se {@code tipo} não corresponder a nenhum
     *     {@link TipoEntidadeImportacao} conhecido
     */
    @Transactional
    public int confirmarImportacao(
            String tipo, List<Map<String, String>> validItems, String actor) {
        int count = 0;
        TipoEntidadeImportacao tipoEntidade = TipoEntidadeImportacao.fromCode(tipo);
        if (tipoEntidade == null) {
            throw new IllegalArgumentException("Tipo de importação desconhecido: " + tipo);
        }
        for (Map<String, String> data : validItems) {
            try {
                switch (tipoEntidade) {
                    case MEMBRO -> importMembro(data, actor);
                    case ORGANIZACAO -> importOrganizacao(data);
                    case EVENTO -> importEvento(data);
                    default -> throw new IllegalArgumentException("Tipo de importação desconhecido: " + tipo);
                }
                count++;
            } catch (Exception e) {
                log.error("Erro ao importar registro de {}: {}", tipo, e.getMessage());
            }
        }
        return count;
    }

    /**
     * Cria o vínculo (membro) do Fiel de um usuário existente a uma organização existente, com papel
     * organizacional informado ou, na ausência, o papel comum {@code MEMBRO_PASTORAL}. Reaproveita
     * {@link AdicionarMembroUseCase} para que o vínculo criado por importação passe pelas mesmas
     * regras de negócio de um vínculo criado manualmente pela UI.
     *
     * @throws IllegalArgumentException se o usuário ou a organização referenciados não existirem
     */
    private void importMembro(Map<String, String> data, String actor) {
        String username = data.get("usuarioUsername").trim();
        String orgNome = data.get("organizacaoNome").trim();
        String papel = Optional.ofNullable(data.get("papel"))
                .filter(p -> !p.isBlank()).orElse("MEMBRO_PASTORAL");
        UUID igrejaId = parseUuid(data.get(KEY_IGREJA_ID));

        var user = usuarioJpaRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado: " + username));
        var org = organizacaoJpaRepository.findByNomeIgnoreCase(orgNome)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organização não encontrada: " + orgNome));

        adicionarMembroUseCase.execute(
                new MembroRequest(
                        user.getFielId(), org.getId(), papel,
                        LocalDate.now(), igrejaId, true),
                actor);
    }

    /**
     * Cria uma organização a partir de uma linha do arquivo. Quando o "parentId" informado não
     * existe (ou não foi informado), {@link #resolveParentId} infere um pai plausível na
     * hierarquia paroquial em vez de rejeitar a linha, para tolerar planilhas incompletas
     * preenchidas manualmente. Da mesma forma, para tipos PASTORAL/MOVIMENTO, o vínculo com o
     * cadastro oficial da pastoral/movimento é resolvido de forma best-effort via
     * {@link #resolvePastoralOficialId}, podendo ficar {@code null} se não houver correspondência.
     */
    private void importOrganizacao(Map<String, String> data) {
        TipoOrganizacao tipoOrg = parseTipoOrganizacao(data.get("tipo"));
        UUID parentId = resolveParentId(tipoOrg, data.get("parentId"));
        UUID pastoralOficialId =
                (tipoOrg == TipoOrganizacao.PASTORAL || tipoOrg == TipoOrganizacao.MOVIMENTO)
                        ? resolvePastoralOficialId(
                        tipoOrg, data.get("pastoralOficialId"), data.get(KEY_NOME))
                        : null;
        organizacaoService.createOrganization(
                data.get(KEY_NOME),
                tipoOrg,
                data.get("contato"),
                data.get("coordenador"),
                data.get("viceCoordenador"),
                parentId,
                pastoralOficialId
        );
    }

    /**
     * Cria um evento a partir de uma linha do arquivo, aplicando a mesma trava de Ano Paroquial
     * Fechado usada no fluxo manual de cadastro de eventos: se o ano civil da data de início
     * estiver com {@link AnoParoquialStatus#FECHADO}, a linha não é rejeitada, mas sofre
     * "Resolução Silenciosa" — vira automaticamente {@code PENDENTE} (com estado/motivo de
     * conflito registrados para homologação posterior) a menos que a linha já traga o status
     * explícito {@code ADICIONADO_EXTRA} com justificativa, único caso em que um evento em ano
     * fechado é aceito diretamente. Isso evita que uma importação em lote precise abortar (ou
     * dar rollback) só porque uma linha cai num ano já fechado. Também resolve o ano/ciclo
     * litúrgico do evento (via {@code liturgicalProvisioningService} ou, na ausência deste, o
     * motor legado {@code liturgicalEngine}) e local/organização responsável com fallback
     * best-effort, análogo ao que ocorre em {@link #importOrganizacao}.
     */
    private void importEvento(Map<String, String> data) {
        EventoEntity entity = new EventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo(data.get(KEY_NOME));
        entity.setDescricao(data.get("descricao"));
        Instant inicio = parseInstant(data.get("dataInicio"));
        Instant fim = parseInstant(data.get("dataFim"));
        entity.setInicioUtc(inicio != null ? inicio : Instant.now());
        entity.setFimUtc(fim != null ? fim : entity.getInicioUtc().plusSeconds(3600));

        // Checar trava de Ano Paroquial Fechado e aplicar Resolução Silenciosa
        int anoCivil = entity.getInicioUtc().atZone(java.time.ZoneOffset.UTC).getYear();
        boolean isAnoFechado = anoParoquialJpaRepository != null &&
                anoParoquialJpaRepository.findByAno(anoCivil)
                        .map(a -> AnoParoquialStatus.FECHADO.equals(a.getStatus()))
                        .orElse(false);

        String inputStatus = Optional.ofNullable(data.get("status")).orElse("APROVADO").toUpperCase();
        if (isAnoFechado) {
            if (EventoStatusInput.ADICIONADO_EXTRA.name().equals(inputStatus)) {
                entity.setStatus(EventoStatusInput.ADICIONADO_EXTRA.name());
                entity.setAdicionadoExtraJustificativa(
                        Optional.ofNullable(data.get("adicionadoExtraJustificativa"))
                                .orElse("Inclusão extra via importação em lote"));
            } else {
                // Resolução Silenciosa: Converte para PENDENTE em ano fechado sem estourar rollback no lote
                entity.setStatus("PENDENTE");
                entity.setEstadoConflito(EstadoConflitoAgenda.PENDENTE);
                entity.setMotivoConflito("Importado em lote durante Ano Paroquial Fechado - aguardando homologação");
            }
        } else {
            entity.setStatus(inputStatus);
        }

        entity.setCategoria(
                Optional.ofNullable(data.get("categoria"))
                        .filter(c -> !c.isBlank())
                        .map(CategoriaEvento::fromValue)
                        .orElse(CategoriaEvento.ADMINISTRATIVO));
        entity.setLocalId(parseUuid(data.get("localId")));
        entity.setIgrejaVinculoId(parseUuid(data.get(KEY_IGREJA_ID)));
        entity.setOrganizacaoResponsavelId(resolveOrganizacaoResponsavelId(data.get("organizacaoId")));

        // Resolução Litúrgica
        if (liturgicalProvisioningService != null) {
            var anoLiturgico = liturgicalProvisioningService.resolveAndEnsure(entity.getInicioUtc());
            entity.setAnoLiturgicoRef(anoLiturgico.getAno());
        } else if (liturgicalEngine != null) {
            int anoLiturgicoRef = liturgicalEngine.resolveAnoLiturgico(entity.getInicioUtc());
            entity.setAnoLiturgicoRef(anoLiturgicoRef);
        }
        if (cicloLiturgicoRepository != null) {
            cicloLiturgicoRepository.findAll().stream()
                    .findFirst()
                    .ifPresent(c -> entity.setCicloLiturgicoId(c.getId()));
        }

        eventoJpaRepository.save(entity);
    }

    /**
     * Resolve o organizacao-pai de uma organização importada: usa o ID informado se ele existir;
     * caso contrário, infere um pai plausível conforme a posição do tipo na hierarquia paroquial
     * (Matriz/Comunidade sob Clero ou Matriz, Conselhos sob Matriz/Comunidade/Clero, Pastoral ou
     * Movimento sob Conselho Pastoral). Clero nunca tem pai. Esse fallback existe para que
     * planilhas sem "parentId" preenchido ainda produzam uma árvore organizacional coerente.
     */
    private UUID resolveParentId(TipoOrganizacao tipo, String inputParentId) {
        UUID id = parseUuid(inputParentId);
        if (id != null) {
            if (organizacaoJpaRepository == null || organizacaoJpaRepository.existsById(id)) {
                return id;
            }
        }

        if (organizacaoJpaRepository == null) {
            return null;
        }
        List<OrganizacaoEntity> all = organizacaoJpaRepository.findAll();
        if (all == null || all.isEmpty()) {
            return null;
        }
        if (tipo == TipoOrganizacao.CLERO) {
            return null;
        }

        if (tipo == TipoOrganizacao.MATRIZ || tipo == TipoOrganizacao.COMUNIDADE) {
            return all.stream()
                    .filter(o -> o.getTipo() == TipoOrganizacao.CLERO || o.getTipo() == TipoOrganizacao.MATRIZ)
                    .map(OrganizacaoEntity::getId)
                    .findFirst()
                    .orElse(all.getFirst().getId());
        }

        if (tipo == TipoOrganizacao.CONSELHO_PASTORAL || tipo == TipoOrganizacao.CONSELHO_ECONOMICO) {
            return all.stream()
                    .filter(o -> o.getTipo() == TipoOrganizacao.MATRIZ
                            || o.getTipo() == TipoOrganizacao.COMUNIDADE
                            || o.getTipo() == TipoOrganizacao.CLERO)
                    .map(OrganizacaoEntity::getId)
                    .findFirst()
                    .orElse(all.getFirst().getId());
        }

        if (tipo == TipoOrganizacao.PASTORAL || tipo == TipoOrganizacao.MOVIMENTO) {
            return all.stream()
                    .filter(o -> o.getTipo() == TipoOrganizacao.CONSELHO_PASTORAL)
                    .map(OrganizacaoEntity::getId)
                    .findFirst()
                    .orElse(all.getFirst().getId());
        }

        return all.getFirst().getId();
    }

    /**
     * Resolve o vínculo com o cadastro oficial de pastoral/movimento: usa o ID informado se
     * existir; caso contrário, tenta encontrar um cadastro oficial do mesmo {@code tipo}. Retorna
     * {@code null} quando nada corresponde — o vínculo oficial é opcional, então a linha ainda
     * assim é importada.
     */
    private UUID resolvePastoralOficialId(TipoOrganizacao tipo, String inputId, String orgNome) {
        UUID id = parseUuid(inputId);
        if (id != null) {
            if (pastoralOficialRepository == null || pastoralOficialRepository.findById(id).isPresent()) {
                return id;
            }
        }

        if (pastoralOficialRepository == null) {
            return null;
        }
        List<PastoralMovimentoOficial> oficiais = pastoralOficialRepository.findAll();
        if (oficiais != null && !oficiais.isEmpty()) {
            Optional<PastoralMovimentoOficial> match = oficiais.stream().filter(p -> p.getTipo() == tipo).findFirst();
            if (match.isPresent()) {
                return match.get().getId();
            }
        }

        return null;
    }

    /**
     * Resolve a organização responsável de um evento importado: usa o ID informado se existir;
     * na ausência/inválido, cai para a primeira organização cadastrada como fallback best-effort,
     * para que a linha ainda seja importada mesmo sem essa referência.
     */
    private UUID resolveOrganizacaoResponsavelId(String inputId) {
        UUID id = parseUuid(inputId);
        if (id != null) {
            if (organizacaoJpaRepository == null || organizacaoJpaRepository.existsById(id)) {
                return id;
            }
        }

        if (organizacaoJpaRepository != null) {
            List<OrganizacaoEntity> all = organizacaoJpaRepository.findAll();
            if (all != null && !all.isEmpty()) {
                return all.getFirst().getId();
            }
        }

        return null;
    }

    private Instant parseInstant(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(input.trim());
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(input.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                    .atZone(ZoneId.systemDefault()).toInstant();
        } catch (Exception ignored) {
        }
        return null;
    }

    private UUID parseUuid(String input) {
        try {
            return UUID.fromString(input.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Converte o texto do campo "tipo" em {@link TipoOrganizacao}; se o valor for inválido ou
     * ausente, cai silenciosamente para {@code PASTORAL} em vez de rejeitar a linha — a
     * validação prévia ({@link #validarOrganizacao}) não checa esse valor, então esse fallback é
     * a única rede de segurança contra um "tipo" mal escrito.
     */
    private TipoOrganizacao parseTipoOrganizacao(String input) {
        try {
            return TipoOrganizacao.valueOf(input.trim().toUpperCase());
        } catch (Exception e) {
            return TipoOrganizacao.PASTORAL;
        }
    }
}

