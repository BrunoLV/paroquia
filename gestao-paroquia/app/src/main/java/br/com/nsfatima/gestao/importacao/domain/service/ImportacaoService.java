package br.com.nsfatima.gestao.importacao.domain.service;

import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoItemResultadoDTO;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoValidacaoResultadoDTO;
import br.com.nsfatima.gestao.importacao.domain.model.TipoEntidadeImportacao;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.application.usecase.CreateMembroUseCase;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
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

@Service
public class ImportacaoService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoService.class);
    private static final String KEY_NOME = "nome";
    private static final String ERR_NOME_REQUIRED = "O campo 'nome' é obrigatório.";
    private static final String KEY_IGREJA_ID = "igrejaId";

    private final CreateMembroUseCase createMembroUseCase;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final OrganizacaoService organizacaoService;
    private final EventoJpaRepository eventoJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizacaoJpaRepository organizacaoJpaRepository;
    private final PastoralMovimentoOficialRepository pastoralOficialRepository;
    private final ImportacaoFileParser importacaoFileParser;
    private final AnoParoquialJpaRepository anoParoquialJpaRepository;
    private final MotorLiturgico liturgicalEngine;
    private final CicloLiturgicoRepository cicloLiturgicoRepository;
    private final ProvisionamentoLiturgicoService liturgicalProvisioningService;

    public ImportacaoService(CreateMembroUseCase createMembroUseCase,
                              MembroJpaRepository membroJpaRepository,
                              UsuarioJpaRepository usuarioJpaRepository,
                              OrganizacaoService organizacaoService,
                              EventoJpaRepository eventoJpaRepository,
                              PasswordEncoder passwordEncoder,
                              OrganizacaoJpaRepository organizacaoJpaRepository,
                              PastoralMovimentoOficialRepository pastoralOficialRepository,
                              ImportacaoFileParser importacaoFileParser,
                              AnoParoquialJpaRepository anoParoquialJpaRepository,
                              MotorLiturgico liturgicalEngine,
                              CicloLiturgicoRepository cicloLiturgicoRepository,
                              ProvisionamentoLiturgicoService liturgicalProvisioningService) {
        this.createMembroUseCase = createMembroUseCase;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.organizacaoService = organizacaoService;
        this.eventoJpaRepository = eventoJpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizacaoJpaRepository = organizacaoJpaRepository;
        this.pastoralOficialRepository = pastoralOficialRepository;
        this.importacaoFileParser = importacaoFileParser;
        this.anoParoquialJpaRepository = anoParoquialJpaRepository;
        this.liturgicalEngine = liturgicalEngine;
        this.cicloLiturgicoRepository = cicloLiturgicoRepository;
        this.liturgicalProvisioningService = liturgicalProvisioningService;
    }

    public byte[] gerarTemplate(String tipo, String formato) throws IOException {
        return importacaoFileParser.gerarTemplate(tipo, formato);
    }

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

    private String validarLinha(String tipo, Map<String, String> data) {
        TipoEntidadeImportacao tipoEntidade = TipoEntidadeImportacao.fromCode(tipo);
        if (tipoEntidade == null) {
            return "Tipo de entidade desconhecido";
        }
        return switch (tipoEntidade) {
            case MEMBRO -> validarMembro(data);
            case USUARIO -> validarUsuario(data);
            case ORGANIZACAO -> validarOrganizacao(data);
            case EVENTO -> validarEvento(data);
        };
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

    private String validarUsuario(Map<String, String> data) {
        if (data.getOrDefault(KEY_NOME, "").isBlank()) {
            return ERR_NOME_REQUIRED;
        }
        if (data.getOrDefault("username", "").isBlank()) {
            return "O campo 'username' é obrigatório.";
        }
        return null;
    }

    private String validarOrganizacao(Map<String, String> data) {
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
                    case USUARIO -> importUsuario(data);
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

    private void importMembro(Map<String, String> data, String actor) {
        String username = data.get("usuarioUsername").trim();
        String orgNome = data.get("organizacaoNome").trim();
        String papel = Optional.ofNullable(data.get("papel"))
                .filter(p -> !p.isBlank()).orElse(TipoEntidadeImportacao.MEMBRO.name());
        UUID igrejaId = parseUuid(data.get(KEY_IGREJA_ID));

        var user = usuarioJpaRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário não encontrado: " + username));
        var org = organizacaoJpaRepository.findByNomeIgnoreCase(orgNome)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organização não encontrada: " + orgNome));

        createMembroUseCase.execute(
                new MembroRequest(
                        user.getId(), org.getId(), papel,
                        LocalDate.now(), igrejaId, true),
                actor);
    }

    private void importUsuario(Map<String, String> data) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(UUID.randomUUID());
        entity.setNome(data.get(KEY_NOME));
        entity.setUsername(data.get("username"));
        entity.setPasswordHash(passwordEncoder.encode("Mudar123!"));
        entity.setTelefone(data.get("telefone"));
        entity.setDataNascimento(parseLocalDate(data.get("dataNascimento")));
        entity.setRoles(
                Optional.ofNullable(data.get("roles"))
                        .filter(r -> !r.isBlank())
                        .orElse("ROLE_USER"));
        entity.setIgrejaId(parseUuid(data.get(KEY_IGREJA_ID)));
        entity.setBatizado(parseBoolean(data.get("batizado")));
        entity.setPrimeiraComunhao(parseBoolean(data.get("primeiraComunhao")));
        entity.setCrismado(parseBoolean(data.get("crismado")));
        entity.setCasadoIgrejaCatolica(parseBoolean(data.get("casadoIgrejaCatolica")));
        entity.setEnabled(true);
        usuarioJpaRepository.save(entity);
    }

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

    private LocalDate parseLocalDate(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        for (DateTimeFormatter fmt : List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"))) {
            try {
                return LocalDate.parse(input.trim(), fmt);
            } catch (Exception ignored) {
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

    private boolean parseBoolean(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        String trimmed = input.trim().toLowerCase();
        return "true".equals(trimmed) || "1".equals(trimmed) || "sim".equals(trimmed) || "s".equals(trimmed);
    }

    private TipoOrganizacao parseTipoOrganizacao(String input) {
        try {
            return TipoOrganizacao.valueOf(input.trim().toUpperCase());
        } catch (Exception e) {
            return TipoOrganizacao.PASTORAL;
        }
    }
}

