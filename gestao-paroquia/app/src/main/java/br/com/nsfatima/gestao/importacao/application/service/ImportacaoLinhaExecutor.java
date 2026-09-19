package br.com.nsfatima.gestao.importacao.application.service;

import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;
import br.com.nsfatima.gestao.calendario.application.service.MotorLiturgico;
import br.com.nsfatima.gestao.calendario.application.service.ProvisionamentoLiturgicoService;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.importacao.domain.model.TipoEntidadeImportacao;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.application.usecase.AdicionarMembroUseCase;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.application.service.GrupoParoquialService;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Responsável pela validação semântica e execução/persistência de linhas individuais de
 * importação para cada tipo de entidade suportado (membro, organização paroquial e evento).
 */
public class ImportacaoLinhaExecutor {

    private static final String KEY_NOME = "nome";
    private static final String ERR_NOME_REQUIRED = "O campo 'nome' é obrigatório.";
    private static final String KEY_IGREJA_ID = "igrejaId";

    private final AdicionarMembroUseCase adicionarMembroUseCase;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final GrupoParoquialService grupoParoquialService;
    private final EventoJpaRepository eventoJpaRepository;
    private final GrupoParoquialJpaRepository grupoParoquialJpaRepository;
    private final UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository;
    private final PastoralMovimentoOficialRepository pastoralOficialRepository;
    private final AnoParoquialJpaRepository anoParoquialJpaRepository;
    private final MotorLiturgico motorLiturgico;
    private final CicloLiturgicoRepository cicloLiturgicoRepository;
    private final ProvisionamentoLiturgicoService provisionamentoLiturgicoService;

    public ImportacaoLinhaExecutor(AdicionarMembroUseCase adicionarMembroUseCase,
                                   UsuarioJpaRepository usuarioJpaRepository,
                                   GrupoParoquialService grupoParoquialService,
                                   EventoJpaRepository eventoJpaRepository,
                                   GrupoParoquialJpaRepository grupoParoquialJpaRepository,
                                   UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository,
                                   PastoralMovimentoOficialRepository pastoralOficialRepository,
                                   AnoParoquialJpaRepository anoParoquialJpaRepository,
                                   MotorLiturgico motorLiturgico,
                                   CicloLiturgicoRepository cicloLiturgicoRepository,
                                   ProvisionamentoLiturgicoService provisionamentoLiturgicoService) {
        this.adicionarMembroUseCase = adicionarMembroUseCase;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.grupoParoquialService = grupoParoquialService;
        this.eventoJpaRepository = eventoJpaRepository;
        this.grupoParoquialJpaRepository = grupoParoquialJpaRepository;
        this.unidadeTerritorialJpaRepository = unidadeTerritorialJpaRepository;
        this.pastoralOficialRepository = pastoralOficialRepository;
        this.anoParoquialJpaRepository = anoParoquialJpaRepository;
        this.motorLiturgico = motorLiturgico;
        this.cicloLiturgicoRepository = cicloLiturgicoRepository;
        this.provisionamentoLiturgicoService = provisionamentoLiturgicoService;
    }

    /**
     * Valida uma linha de dados isoladamente de acordo com o tipo de entidade informado.
     *
     * @param tipo código da entidade a importar
     * @param data mapa com os campos da linha
     * @return mensagem de erro se inválido, ou {@code null} se a linha é aceitável
     */
    public String validarLinha(String tipo, Map<String, String> data) {
        TipoEntidadeImportacao tipoEntidade = TipoEntidadeImportacao.fromCode(tipo);
        if (tipoEntidade == null) {
            return "Tipo de entidade desconhecido";
        }
        return switch (tipoEntidade) {
            case MEMBRO -> validarMembro(data);
            case GRUPO_PAROQUIAL -> validarGrupoParoquial(data);
            case EVENTO -> validarEvento(data);
        };
    }

    /**
     * Processa e persiste a entidade de acordo com seu tipo correspondente.
     *
     * @param tipoEntidade enum de tipo
     * @param data campos da linha
     * @param actor autor da importação
     */
    public void processarImportacao(TipoEntidadeImportacao tipoEntidade, Map<String, String> data, String actor) {
        switch (tipoEntidade) {
            case MEMBRO -> importMembro(data, actor);
            case GRUPO_PAROQUIAL -> importGrupoParoquial(data);
            case EVENTO -> importEvento(data);
            default -> throw new IllegalArgumentException("Tipo de importação desconhecido: " + tipoEntidade);
        }
    }

    private String validarMembro(Map<String, String> data) {
        String username = data.getOrDefault("usuarioUsername", "").trim();
        if (username.isBlank()) {
            return "O campo 'usuarioUsername' é obrigatório.";
        }
        var usuarioOpt = usuarioJpaRepository.findByUsernameIgnoreCase(username);
        if (usuarioOpt.isEmpty()) {
            return "Usuário não encontrado: " + username;
        }

        String orgNome = data.getOrDefault("grupoParoquialNome", "").trim();
        if (orgNome.isBlank()) {
            return "O campo 'grupoParoquialNome' é obrigatório.";
        }
        var orgOpt = grupoParoquialJpaRepository.findByNomeIgnoreCase(orgNome);
        if (orgOpt.isEmpty()) {
            return "Organização não encontrada: " + orgNome;
        }

        if (data.getOrDefault("papel", "").isBlank()) {
            return "O campo 'papel' é obrigatório.";
        }
        return null;
    }

    private String validarGrupoParoquial(Map<String, String> data) {
        if (data.getOrDefault(KEY_NOME, "").isBlank()) {
            return ERR_NOME_REQUIRED;
        }
        if (data.getOrDefault("tipo", "").isBlank()) {
            return "O campo 'tipo' é obrigatório.";
        }
        return null;
    }

    private String validarEvento(Map<String, String> data) {
        if (data.getOrDefault(KEY_NOME, "").isBlank()) {
            return ERR_NOME_REQUIRED;
        }
        if (data.getOrDefault("dataInicio", "").isBlank()) {
            return "O campo 'dataInicio' é obrigatório.";
        }
        return null;
    }

    private void importMembro(Map<String, String> data, String actor) {
        String username = data.get("usuarioUsername").trim();
        String orgNome = data.get("grupoParoquialNome").trim();
        String papel = Optional.ofNullable(data.get("papel"))
                .filter(p -> !p.isBlank()).orElse("MEMBRO_PASTORAL");
        UUID igrejaId = parseUuid(data.get(KEY_IGREJA_ID));

        var user = usuarioJpaRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado: " + username));
        var org = grupoParoquialJpaRepository.findByNomeIgnoreCase(orgNome)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organização não encontrada: " + orgNome));

        adicionarMembroUseCase.execute(
                new MembroRequest(
                        user.getFielId(), org.getId(), papel,
                        LocalDate.now(), igrejaId, true),
                actor);
    }

    private void importGrupoParoquial(Map<String, String> data) {
        TipoGrupo tipoOrg = parseTipoGrupo(data.get("tipo"));
        UUID parentId = resolveParentId(tipoOrg, data.get("parentId"));
        UUID pastoralOficialId =
                (tipoOrg == TipoGrupo.PASTORAL || tipoOrg == TipoGrupo.MOVIMENTO)
                        ? resolvePastoralOficialId(
                        tipoOrg, data.get("pastoralOficialId"), data.get(KEY_NOME))
                        : null;
        grupoParoquialService.criar(
                data.get(KEY_NOME),
                tipoOrg,
                data.get("contato"),
                data.get("coordenador"),
                data.get("viceCoordenador"),
                parentId,
                pastoralOficialId
        );
    }

    private void importEvento(Map<String, String> data) {
        EventoEntity entity = new EventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo(data.get(KEY_NOME));
        entity.setDescricao(data.get("descricao"));
        Instant inicio = parseInstant(data.get("dataInicio"));
        Instant fim = parseInstant(data.get("dataFim"));
        entity.setInicioUtc(inicio != null ? inicio : Instant.now());
        entity.setFimUtc(fim != null ? fim : entity.getInicioUtc().plusSeconds(3600));

        int anoCivil = entity.getInicioUtc().atZone(ZoneOffset.UTC).getYear();
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
        entity.setGrupoParoquialResponsavelId(resolveGrupoParoquialResponsavelId(data.get("grupoParoquialId")));

        if (provisionamentoLiturgicoService != null) {
            var anoLiturgico = provisionamentoLiturgicoService.resolveAndEnsure(entity.getInicioUtc());
            entity.setAnoLiturgicoRef(anoLiturgico.getAno());
        } else if (motorLiturgico != null) {
            int anoLiturgicoRef = motorLiturgico.resolveAnoLiturgico(entity.getInicioUtc());
            entity.setAnoLiturgicoRef(anoLiturgicoRef);
        }
        if (cicloLiturgicoRepository != null) {
            cicloLiturgicoRepository.findAll().stream()
                    .findFirst()
                    .ifPresent(c -> entity.setCicloLiturgicoId(c.getId()));
        }

        eventoJpaRepository.save(entity);
    }

    private UUID resolveParentId(TipoGrupo tipo, String inputParentId) {
        UUID id = parseUuid(inputParentId);
        if (id != null) {
            if (grupoParoquialJpaRepository == null || grupoParoquialJpaRepository.existsById(id)) {
                return id;
            }
        }

        if (grupoParoquialJpaRepository == null) {
            return null;
        }
        List<GrupoParoquialEntity> all = grupoParoquialJpaRepository.findAll();
        if (all == null || all.isEmpty()) {
            return null;
        }

        if (tipo == TipoGrupo.CONSELHO) {
            if (unidadeTerritorialJpaRepository == null) {
                return null;
            }
            List<UnidadeTerritorialEntity> unidades = unidadeTerritorialJpaRepository.findAll();
            return unidades.stream()
                    .filter(u -> u.getTipo() == TipoUnidadeTerritorial.MATRIZ
                            || u.getTipo() == TipoUnidadeTerritorial.COMUNIDADE)
                    .map(UnidadeTerritorialEntity::getId)
                    .findFirst()
                    .orElse(null);
        }

        if (tipo == TipoGrupo.PASTORAL || tipo == TipoGrupo.MOVIMENTO) {
            return all.stream()
                    .filter(o -> o.getTipo() == TipoGrupo.CONSELHO)
                    .map(GrupoParoquialEntity::getId)
                    .findFirst()
                    .orElse(all.getFirst().getId());
        }

        return null;
    }

    private UUID resolvePastoralOficialId(TipoGrupo tipo, String inputId, String orgNome) {
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

    private UUID resolveGrupoParoquialResponsavelId(String inputId) {
        UUID id = parseUuid(inputId);
        if (id != null) {
            if (grupoParoquialJpaRepository == null || grupoParoquialJpaRepository.existsById(id)) {
                return id;
            }
        }

        if (grupoParoquialJpaRepository != null) {
            List<GrupoParoquialEntity> all = grupoParoquialJpaRepository.findAll();
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

    private TipoGrupo parseTipoGrupo(String input) {
        try {
            return TipoGrupo.valueOf(input.trim().toUpperCase());
        } catch (Exception e) {
            return TipoGrupo.PASTORAL;
        }
    }
}
