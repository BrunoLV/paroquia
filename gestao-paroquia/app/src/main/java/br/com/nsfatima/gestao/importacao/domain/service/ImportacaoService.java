package br.com.nsfatima.gestao.importacao.domain.service;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoItemResultadoDTO;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoValidacaoResultadoDTO;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.application.usecase.CreateMembroUseCase;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.domain.service.OrganizacaoService;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ImportacaoService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final CreateMembroUseCase createMembroUseCase;
    private final MembroJpaRepository membroJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final OrganizacaoService organizacaoService;
    private final EventoJpaRepository eventoJpaRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizacaoJpaRepository organizacaoJpaRepository;
    private final PastoralMovimentoOficialRepository pastoralOficialRepository;

    public ImportacaoService(CreateMembroUseCase createMembroUseCase,
                             MembroJpaRepository membroJpaRepository,
                             UsuarioJpaRepository usuarioJpaRepository,
                             OrganizacaoService organizacaoService,
                             EventoJpaRepository eventoJpaRepository,
                             PasswordEncoder passwordEncoder,
                             OrganizacaoJpaRepository organizacaoJpaRepository,
                             PastoralMovimentoOficialRepository pastoralOficialRepository) {
        this.createMembroUseCase = createMembroUseCase;
        this.membroJpaRepository = membroJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.organizacaoService = organizacaoService;
        this.eventoJpaRepository = eventoJpaRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizacaoJpaRepository = organizacaoJpaRepository;
        this.pastoralOficialRepository = pastoralOficialRepository;
    }

    public byte[] gerarTemplate(String tipo, String formato) throws IOException {
        String headers = getHeadersParaTipo(tipo);
        List<String> sampleRows = getSampleRowsParaTipo(tipo);

        if ("XLSX".equalsIgnoreCase(formato) || "XLS".equalsIgnoreCase(formato)) {
            try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Template_" + tipo);
                Row headerRow = sheet.createRow(0);
                String[] cols = headers.split(",");
                for (int i = 0; i < cols.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(cols[i]);
                }

                for (int r = 0; r < sampleRows.size(); r++) {
                    Row row = sheet.createRow(r + 1);
                    String[] rowVals = sampleRows.get(r).split(",");
                    for (int c = 0; c < rowVals.length; c++) {
                        Cell cell = row.createCell(c);
                        cell.setCellValue(rowVals[c]);
                    }
                }

                workbook.write(out);
                return out.toByteArray();
            }
        }

        StringBuilder csv = new StringBuilder(headers).append("\n");
        for (String sample : sampleRows) {
            csv.append(sample).append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static final String TYPE_MEMBRO = "MEMBRO";
    private static final String TYPE_USUARIO = "USUARIO";
    private static final String TYPE_ORGANIZACAO = "ORGANIZACAO";
    private static final String TYPE_EVENTO = "EVENTO";
    private static final String KEY_NOME = "nome";
    private static final String ERR_NOME_REQUIRED = "O campo 'nome' é obrigatório.";
    private static final String KEY_IGREJA_ID = "igrejaId";

    private String getHeadersParaTipo(String tipo) {
        return switch (tipo.toUpperCase()) {
            case TYPE_MEMBRO -> "usuarioUsername,organizacaoNome,papel,igrejaId";
            case TYPE_USUARIO -> "nome,username,telefone,dataNascimento,igrejaId,roles,"
                    + "batizado,primeiraComunhao,crismado,casadoIgrejaCatolica";
            case TYPE_ORGANIZACAO -> "nome,tipo,contato,coordenador,viceCoordenador,parentId,pastoralOficialId";
            case TYPE_EVENTO -> "nome,descricao,dataInicio,dataFim,categoria,localId,igrejaId,organizacaoId";
            default -> throw new IllegalArgumentException("Tipo de importação inválido: " + tipo);
        };
    }

    private List<String> getSampleRowsParaTipo(String tipo) {
        return switch (tipo.toUpperCase()) {
            case TYPE_MEMBRO -> List.of(
                    "joao.silva@email.com,Pastoral da Comunicação,COORDENADOR,",
                    "maria.souza@email.com,Pastoral da Catequese,MEMBRO,"
            );
            case TYPE_USUARIO -> List.of(
                    "Carlos Eduardo,carlos.eduardo@email.com,11977776666,1988-03-10,,ROLE_USER,true,true,true,false",
                    "Ana Beatriz,ana.beatriz@email.com,11966665555,1995-11-25,,ROLE_USER,true,true,false,false"
            );
            case TYPE_ORGANIZACAO -> List.of(
                    "Pastoral da Música,PASTORAL,musica@paroquia.org,Carlos Silva,Fernanda Lima,,",
                    "Grupo de Jovens,MOVIMENTO,jovens@paroquia.org,Lucas Mendes,Patricia Rocha,,"
            );
            case TYPE_EVENTO -> List.of(
                    "Missa Dominical,Missa Solene com a Comunidade,2026-08-02 09:00,2026-08-02 10:30,LITURGICO,,,",
                    "Encontro da Catequese,Formação sobre Sacramentos,2026-08-05 14:00,2026-08-05 16:00,FORMATIVO,,,"
            );
            default -> Collections.emptyList();
        };
    }

    public ImportacaoValidacaoResultadoDTO validarArquivo(String tipo, MultipartFile file) throws IOException {
        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();
        List<Map<String, String>> rows;
        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            rows = parseExcel(file.getInputStream());
        } else {
            rows = parseCsv(file.getInputStream());
        }

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

    private List<Map<String, String>> parseCsv(InputStream is) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return rows;
            }
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }
            String[] headers = headerLine.split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] values = line.split(",", -1);
                Map<String, String> map = new LinkedHashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    map.put(headers[i].trim(), (i < values.length) ? values[i].trim() : "");
                }
                rows.add(map);
            }
        }
        return rows;
    }

    private List<Map<String, String>> parseExcel(InputStream is) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return rows;
            }
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                String h = getCellValueAsString(cell).trim();
                if (h.startsWith("\uFEFF")) {
                    h = h.substring(1);
                }
                headers.add(h);
            }
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                Map<String, String> map = new LinkedHashMap<>();
                boolean empty = true;
                for (int c = 0; c < headers.size(); c++) {
                    String val = getCellValueAsString(row.getCell(c)).trim();
                    if (!val.isEmpty()) {
                        empty = false;
                    }
                    map.put(headers.get(c), val);
                }
                if (!empty) {
                    rows.add(map);
                }
            }
        }
        return rows;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                yield (num == Math.floor(num)) ? String.valueOf((long) num) : String.valueOf(num);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private String validarLinha(String tipo, Map<String, String> data) {
        return switch (tipo.toUpperCase()) {
            case "MEMBRO" -> validarMembro(data);
            case "USUARIO" -> validarUsuario(data);
            case "ORGANIZACAO" -> validarOrganizacao(data);
            case "EVENTO" -> validarEvento(data);
            default -> "Tipo de entidade desconhecido";
        };
    }

    private String validarMembro(Map<String, String> data) {
        String username = data.getOrDefault("usuarioUsername", "").trim();
        if (username.isBlank()) {
            return "O campo 'usuarioUsername' é obrigatório.";
        }
        var userOpt = usuarioJpaRepository.findByUsernameIgnoreCase(username);
        if (userOpt.isEmpty()) {
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
        for (Map<String, String> data : validItems) {
            try {
                switch (tipo.toUpperCase()) {
                    case TYPE_MEMBRO -> {
                        String username = data.get("usuarioUsername").trim();
                        String orgNome = data.get("organizacaoNome").trim();
                        String papel = Optional.ofNullable(data.get("papel"))
                                .filter(p -> !p.isBlank()).orElse(TYPE_MEMBRO);
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
                        count++;
                    }
                    case TYPE_USUARIO -> {
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
                        count++;
                    }
                    case TYPE_ORGANIZACAO -> {
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
                        count++;
                    }
                    case TYPE_EVENTO -> {
                        EventoEntity entity = new EventoEntity();
                        entity.setId(UUID.randomUUID());
                        entity.setTitulo(data.get(KEY_NOME));
                        entity.setDescricao(data.get("descricao"));
                        Instant inicio = parseInstant(data.get("dataInicio"));
                        Instant fim = parseInstant(data.get("dataFim"));
                        entity.setInicioUtc(inicio != null ? inicio : Instant.now());
                        entity.setFimUtc(fim != null ? fim : entity.getInicioUtc().plusSeconds(3600));
                        entity.setStatus("APROVADO");
                        entity.setCategoria(
                                Optional.ofNullable(data.get("categoria"))
                                        .filter(c -> !c.isBlank()).orElse("GERAL"));
                        entity.setLocalId(parseUuid(data.get("localId")));
                        entity.setIgrejaVinculoId(parseUuid(data.get(KEY_IGREJA_ID)));
                        entity.setOrganizacaoResponsavelId(resolveOrganizacaoResponsavelId(data.get("organizacaoId")));
                        eventoJpaRepository.save(entity);
                        count++;
                    }
                    default -> throw new IllegalArgumentException("Tipo de importação desconhecido: " + tipo);
                }
            } catch (Exception e) {
                log.error("Erro ao importar registro de {}: {}", tipo, e.getMessage(), e);
            }
        }
        return count;
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
                    .orElse(all.get(0).getId());
        }

        if (tipo == TipoOrganizacao.CONSELHO_PASTORAL || tipo == TipoOrganizacao.CONSELHO_ECONOMICO) {
            return all.stream()
                    .filter(o -> o.getTipo() == TipoOrganizacao.MATRIZ
                            || o.getTipo() == TipoOrganizacao.COMUNIDADE
                            || o.getTipo() == TipoOrganizacao.CLERO)
                    .map(OrganizacaoEntity::getId)
                    .findFirst()
                    .orElse(all.get(0).getId());
        }

        if (tipo == TipoOrganizacao.PASTORAL || tipo == TipoOrganizacao.MOVIMENTO) {
            return all.stream()
                    .filter(o -> o.getTipo() == TipoOrganizacao.CONSELHO_PASTORAL)
                    .map(OrganizacaoEntity::getId)
                    .findFirst()
                    .orElse(all.get(0).getId());
        }

        return all.get(0).getId();
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
                return all.get(0).getId();
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
