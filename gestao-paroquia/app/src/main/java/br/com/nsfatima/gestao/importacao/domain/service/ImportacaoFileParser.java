package br.com.nsfatima.gestao.importacao.domain.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ImportacaoFileParser {

    private static final String TYPE_MEMBRO = "MEMBRO";
    private static final String TYPE_USUARIO = "USUARIO";
    private static final String TYPE_ORGANIZACAO = "ORGANIZACAO";
    private static final String TYPE_EVENTO = "EVENTO";

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

    public List<Map<String, String>> parseFile(String filename, InputStream inputStream) throws IOException {
        if (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls")) {
            return parseExcel(inputStream);
        }
        return parseCsv(inputStream);
    }

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
}
