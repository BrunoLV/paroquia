package br.com.nsfatima.gestao.calendario.infrastructure.export;

import br.com.nsfatima.gestao.calendario.application.export.ExportadorCalendario;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Exporta uma lista de eventos do calendário para uma planilha Excel (.xlsx) usando Apache POI,
 * incluindo logo da paróquia, título, período e uma tabela de eventos com colunas formatadas.
 */
@Component
public class ExportadorCalendarioExcel implements ExportadorCalendario {

    private static final ZoneId BR_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Indica se este exportador atende ao formato solicitado.
     *
     * @param formato identificador do formato de exportação (ex.: {@code "EXCEL"})
     * @return {@code true} se o formato, ignorando caixa, for "EXCEL"
     */
    @Override
    public boolean suporta(String formato) {
        return "EXCEL".equalsIgnoreCase(formato);
    }

    /**
     * Gera a planilha Excel com logo (se disponível), título, período do relatório e uma tabela
     * com cabeçalho estilizado seguida das linhas de eventos, resolvendo nomes de local e
     * organização a partir dos mapas fornecidos e ajustando a largura das colunas ao final.
     *
     * @param eventos eventos a serem exportados
     * @param localMap mapa de ID de local para nome de exibição
     * @param orgMap mapa de ID de organização para nome de exibição
     * @param start início do período exibido no subtítulo do relatório
     * @param end fim do período exibido no subtítulo do relatório
     * @param config configuração da paróquia, usada para nome e logo da paróquia
     * @return bytes do arquivo .xlsx gerado
     * @throws RuntimeException se ocorrer erro de E/S na serialização do workbook
     */
    @Override
    public byte[] exportar(List<EventoEntity> eventos,
                           Map<UUID, String> localMap,
                           Map<UUID, String> orgMap,
                           Instant start,
                           Instant end,
                           ParoquiaConfiguracaoEntity config) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Calendário");

            // Insere o logo da paróquia da configuração do banco de dados na área A1:B4
            byte[] logoBytes = config != null ? config.getLogoBytes() : null;
            if (logoBytes != null && logoBytes.length > 0) {
                try {
                    int pictureIdx = workbook.addPicture(logoBytes, Workbook.PICTURE_TYPE_PNG);
                    CreationHelper helper = workbook.getCreationHelper();
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(0);
                    anchor.setRow1(0);
                    anchor.setCol2(2); // Colunas A e B (fim exclusivo)
                    anchor.setRow2(4); // Linhas 0 a 3 (fim exclusivo)
                    drawing.createPicture(anchor, pictureIdx);
                } catch (Exception ex) {
                    // Ignore image loading errors to keep generation robust
                }
            }

            // Título e Subtítulo ao lado do Logo
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            Row rowTitle = sheet.createRow(1);
            Cell cellTitle = rowTitle.createCell(2); // Coluna C
            cellTitle.setCellValue("Relatório do Calendário de Eventos");
            cellTitle.setCellStyle(titleStyle);

            CellStyle periodStyle = workbook.createCellStyle();
            Font periodFont = workbook.createFont();
            periodFont.setItalic(true);
            periodFont.setFontHeightInPoints((short) 10);
            periodStyle.setFont(periodFont);

            Row rowPeriod = sheet.createRow(2);
            Cell cellPeriod = rowPeriod.createCell(2); // Coluna C
            cellPeriod.setCellValue("Período: " + formatInstantDateOnly(start) + " a " + formatInstantDateOnly(end));
            cellPeriod.setCellStyle(periodStyle);

            // Estilos do Cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Cabeçalho na linha 4
            Row headerRow = sheet.createRow(4);
            String[] columns = {"Título",
                    "Descrição", "Categoria", "Início", "Fim", "Local", "Organização Responsável", "Status"};

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Dados na linha 5 em diante
            int rowIdx = 5;
            for (EventoEntity e : eventos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getTitulo());
                row.createCell(1).setCellValue(e.getDescricao() != null ? e.getDescricao() : "");
                row.createCell(2).setCellValue(e.getCategoria() != null ? e.getCategoria().name() : "");
                row.createCell(3).setCellValue(formatInstant(e.getInicioUtc()));
                row.createCell(4).setCellValue(formatInstant(e.getFimUtc()));
                row.createCell(5).setCellValue(localMap.getOrDefault(e.getLocalId(), "N/A"));
                row.createCell(6).setCellValue(orgMap.getOrDefault(e.getGrupoParoquialResponsavelId(), "N/A"));
                row.createCell(7).setCellValue(e.getStatus());
            }

            // Redimensiona colunas
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
                // Garantir largura mínima para as colunas A e B para acomodar o logo
                if (i < 2) {
                    int currentWidth = sheet.getColumnWidth(i);
                    if (currentWidth < 4500) {
                        sheet.setColumnWidth(i, 4500);
                    }
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar arquivo Excel", e);
        }
    }

    /**
     * Formata um instante UTC como data/hora no fuso de São Paulo, no padrão {@code dd/MM/yyyy HH:mm}.
     *
     * @param instant instante a formatar, pode ser {@code null}
     * @return string formatada, ou vazia se o instante for {@code null}
     */
    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        return ZonedDateTime.ofInstant(instant, BR_ZONE).format(DATE_FORMATTER);
    }

    /**
     * Formata um instante UTC apenas como data (sem hora) no fuso de São Paulo, no padrão
     * {@code dd/MM/yyyy}.
     *
     * @param instant instante a formatar, pode ser {@code null}
     * @return string formatada, ou vazia se o instante for {@code null}
     */
    private String formatInstantDateOnly(Instant instant) {
        if (instant == null) {
            return "";
        }
        return ZonedDateTime.ofInstant(instant, BR_ZONE).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
