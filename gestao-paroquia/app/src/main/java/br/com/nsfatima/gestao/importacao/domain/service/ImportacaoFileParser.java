package br.com.nsfatima.gestao.importacao.domain.service;

import br.com.nsfatima.gestao.importacao.domain.model.TipoEntidadeImportacao;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lida com o formato de arquivo (CSV/XLSX) do fluxo de importação em lote: gera o template de
 * exemplo por tipo de entidade e faz o parse de um arquivo enviado para uma lista de linhas
 * (mapa coluna→valor), agnóstica das regras de negócio de cada {@link TipoEntidadeImportacao} —
 * essas ficam em {@code ImportacaoService}. Convenções de arquivo: primeira linha é sempre o
 * cabeçalho (nomes de coluna), célula/valor ausente vira string vazia (nunca {@code null}), e um
 * eventual BOM UTF-8 no início do arquivo/planilha é descartado ao ler o cabeçalho.
 */
@Component
public class ImportacaoFileParser {

    /**
     * Monta um arquivo de exemplo (cabeçalho + linhas de amostra) para o tipo de entidade
     * informado, no formato solicitado.
     *
     * @param tipo código de {@link TipoEntidadeImportacao}
     * @param formato "XLSX"/"XLS" gera planilha; qualquer outro valor gera CSV
     * @return bytes do arquivo gerado
     * @throws IOException se houver falha ao escrever a planilha
     * @throws IllegalArgumentException se {@code tipo} não corresponder a nenhum
     *     {@link TipoEntidadeImportacao} conhecido
     */
    public byte[] gerarTemplate(String tipo, String formato) throws IOException {
        TipoEntidadeImportacao tipoEntidade = TipoEntidadeImportacao.fromCode(tipo);
        if (tipoEntidade == null) {
            throw new IllegalArgumentException("Tipo de importação inválido: " + tipo);
        }
        String headers = getHeadersParaTipo(tipoEntidade);
        List<String> sampleRows = getSampleRowsParaTipo(tipoEntidade);

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

    /**
     * Faz o parse do arquivo enviado pelo usuário para uma lista de linhas (mapa coluna→valor),
     * escolhendo o parser (Excel ou CSV) pela extensão do nome do arquivo, não pelo
     * content-type — o content-type de upload é pouco confiável entre navegadores/clientes.
     *
     * @param filename nome original do arquivo enviado, usado só para decidir o formato
     * @param inputStream conteúdo do arquivo
     * @return uma entrada por linha de dados (cabeçalho excluído), preservando a ordem do arquivo
     * @throws IOException se o arquivo não puder ser lido/parseado
     */
    public List<Map<String, String>> parseFile(String filename, InputStream inputStream) throws IOException {
        if (filename.toLowerCase().endsWith(".xlsx") || filename.toLowerCase().endsWith(".xls")) {
            return parseExcel(inputStream);
        }
        return parseCsv(inputStream);
    }

    /**
     * Cabeçalho de colunas esperado por tipo de entidade — precisa ficar em sincronia com os
     * nomes de campo lidos em {@code ImportacaoService} (ex.: "usuarioUsername",
     * "organizacaoNome") e com os que a validação por linha exige.
     */
    private String getHeadersParaTipo(TipoEntidadeImportacao tipo) {
        return switch (tipo) {
            case MEMBRO -> "usuarioUsername,organizacaoNome,papel,igrejaId";
            case ORGANIZACAO -> "nome,tipo,contato,coordenador,viceCoordenador,parentId,pastoralOficialId";
            case EVENTO -> "nome,descricao,dataInicio,dataFim,categoria,localId,igrejaId,organizacaoId";
        };
    }

    /** Linhas de exemplo preenchidas no template, ilustrando um preenchimento válido por tipo. */
    private List<String> getSampleRowsParaTipo(TipoEntidadeImportacao tipo) {
        return switch (tipo) {
            case MEMBRO -> List.of(
                    "joao.silva@email.com,Pastoral da Comunicação,COORDENADOR,",
                    "maria.souza@email.com,Pastoral da Catequese,MEMBRO,"
            );
            case ORGANIZACAO -> List.of(
                    "Pastoral da Música,PASTORAL,musica@paroquia.org,Carlos Silva,Fernanda Lima,,",
                    "Grupo de Jovens,MOVIMENTO,jovens@paroquia.org,Lucas Mendes,Patricia Rocha,,"
            );
            case EVENTO -> List.of(
                    "Missa Dominical,Missa Solene com a Comunidade,2026-08-02 09:00,2026-08-02 10:30,LITURGICO,,,",
                    "Encontro da Catequese,Formação sobre Sacramentos,2026-08-05 14:00,2026-08-05 16:00,FORMATIVO,,,"
            );
        };
    }

    /**
     * Parser de CSV simples (split por vírgula, sem suporte a campos entre aspas). Linhas em
     * branco são ignoradas silenciosamente (fim de arquivo/planilha com linhas vazias no fim é
     * comum em exports manuais). Valores ausentes em uma linha mais curta que o cabeçalho viram
     * string vazia, nunca {@code null} — mantém o contrato de {@link #parseFile}.
     */
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

    /**
     * Parser de XLS/XLSX: lê apenas a primeira planilha do arquivo. Uma linha é descartada
     * quando todas as suas células ficam vazias (evita linhas fantasma que o Excel às vezes
     * mantém no fim de uma planilha editada manualmente).
     */
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

    /**
     * Normaliza qualquer célula do Excel para {@code String}, unificando o tipo de dado com o
     * CSV (onde tudo já é texto). Números inteiros não ganham casas decimais espúrias
     * (ex.: "5" e não "5.0"), e células de data são convertidas para o texto do
     * {@code LocalDateTime}, para casar com os formatos aceitos pelos parsers de data/hora do
     * {@code ImportacaoService}.
     */
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
