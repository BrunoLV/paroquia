package br.com.nsfatima.gestao.importacao.domain.service;

import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoItemResultadoDTO;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoValidacaoResultadoDTO;
import br.com.nsfatima.gestao.membro.application.usecase.CreateMembroUseCase;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ImportacaoService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private final CreateMembroUseCase createMembroUseCase;
    private final MembroJpaRepository membroJpaRepository;

    public ImportacaoService(CreateMembroUseCase createMembroUseCase, MembroJpaRepository membroJpaRepository) {
        this.createMembroUseCase = createMembroUseCase;
        this.membroJpaRepository = membroJpaRepository;
    }

    public byte[] gerarTemplate(String tipo, String formato) throws IOException {
        String headers = getHeadersParaTipo(tipo);
        if ("XLSX".equalsIgnoreCase(formato) || "XLS".equalsIgnoreCase(formato)) {
            try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Template_" + tipo);
                Row headerRow = sheet.createRow(0);
                String[] cols = headers.split(",");
                for (int i = 0; i < cols.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(cols[i]);
                }
                workbook.write(out);
                return out.toByteArray();
            }
        }
        // Default to CSV
        return (headers + "\n").getBytes(StandardCharsets.UTF_8);
    }

    private String getHeadersParaTipo(String tipo) {
        return switch (tipo.toUpperCase()) {
            case "MEMBRO" -> "nomeCompleto,email,telefone,dataNascimento,rua,numero,bairro,cidade";
            case "USUARIO" -> "nome,username,telefone,dataNascimento,igrejaId,roles";
            case "ORGANIZACAO" -> "nome,tipo,contato,coordenador,viceCoordenador,parentId,pastoralOficialId";
            case "EVENTO" -> "nome,descricao,dataInicio,dataFim,categoria,localId,igrejaId,organizacaoId";
            default -> throw new IllegalArgumentException("Tipo de importação inválido: " + tipo);
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
            int linhaNum = i + 2; // Line 1 is header
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
            if (headerLine == null) return rows;
            String[] headers = headerLine.split(",");

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split(",", -1);
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    String val = (i < values.length) ? values[i].trim() : "";
                    map.put(headers[i].trim(), val);
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
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) return rows;

            Row headerRow = rowIterator.next();
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim());
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> map = new HashMap<>();
                boolean empty = true;
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String val = getCellValueAsString(cell);
                    if (!val.isEmpty()) empty = false;
                    map.put(headers.get(i), val);
                }
                if (!empty) {
                    rows.add(map);
                }
            }
        }
        return rows;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    double val = cell.getNumericCellValue();
                    if (val == (long) val) {
                        yield String.valueOf((long) val);
                    }
                    yield String.valueOf(val);
                }
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
        String nome = data.get("nomeCompleto");
        if (nome == null || nome.trim().isEmpty()) {
            return "O campo 'nomeCompleto' é obrigatório.";
        }
        String email = data.get("email");
        if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "O formato do e-mail é inválido.";
        }
        return null;
    }

    private String validarUsuario(Map<String, String> data) {
        String nome = data.get("nome");
        String username = data.get("username");
        if (nome == null || nome.trim().isEmpty()) return "O campo 'nome' é obrigatório.";
        if (username == null || username.trim().isEmpty()) return "O campo 'username' é obrigatório.";
        return null;
    }

    private String validarOrganizacao(Map<String, String> data) {
        String nome = data.get("nome");
        String tipo = data.get("tipo");
        if (nome == null || nome.trim().isEmpty()) return "O campo 'nome' é obrigatório.";
        if (tipo == null || tipo.trim().isEmpty()) return "O campo 'tipo' é obrigatório.";
        return null;
    }

    private String validarEvento(Map<String, String> data) {
        String nome = data.get("nome");
        String dataInicio = data.get("dataInicio");
        if (nome == null || nome.trim().isEmpty()) return "O campo 'nome' é obrigatório.";
        if (dataInicio == null || dataInicio.trim().isEmpty()) return "O campo 'dataInicio' é obrigatório.";
        return null;
    }

    public int confirmarImportacao(String tipo, List<Map<String, String>> validItems, String actor) {
        int count = 0;
        for (Map<String, String> data : validItems) {
            if ("MEMBRO".equalsIgnoreCase(tipo)) {
                try {
                    String nome = data.get("nomeCompleto");
                    String email = data.get("email");
                    String tel = data.get("telefone");
                    String dtStr = data.get("dataNascimento");
                    LocalDate dt = (dtStr != null && !dtStr.isBlank()) ? LocalDate.parse(dtStr, DateTimeFormatter.ISO_LOCAL_DATE) : null;

                    String endereco = String.join(", ",
                            Optional.ofNullable(data.get("rua")).orElse(""),
                            Optional.ofNullable(data.get("numero")).orElse(""),
                            Optional.ofNullable(data.get("bairro")).orElse(""),
                            Optional.ofNullable(data.get("cidade")).orElse("")
                    ).replaceAll("^, |, $|(?<=, ), ", "").trim();

                    MembroRequest req = new MembroRequest(
                            nome, dt, email, tel,
                            endereco.isBlank() ? null : endereco,
                            null, null, null, null, null
                    );
                    createMembroUseCase.execute(req, actor);
                    count++;
                } catch (Exception e) {
                    // Ignore or log error for single failed record
                }
            } else {
                count++;
            }
        }
        return count;
    }
}
