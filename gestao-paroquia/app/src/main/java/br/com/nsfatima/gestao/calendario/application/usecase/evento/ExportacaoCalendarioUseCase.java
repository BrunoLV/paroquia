package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Image;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfGState;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.ClientAnchor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.awt.Color;

@Service
public class ExportacaoCalendarioUseCase {

    private final EventoJpaRepository eventRepository;
    private final LocalJpaRepository localRepository;
    private final OrganizacaoJpaRepository organizationRepository;

    private static final ZoneId BR_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public ExportacaoCalendarioUseCase(
            EventoJpaRepository eventRepository,
            LocalJpaRepository localRepository,
            OrganizacaoJpaRepository organizationRepository) {
        this.eventRepository = eventRepository;
        this.localRepository = localRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional(readOnly = true)
    public byte[] execute(String formato, String opcao, Integer ano, Integer mes, Instant dataInicio, Instant dataFim,
                          List<String> categorias, List<UUID> organizacaoIds) {
        // 1. Determina o período de tempo (UTC)
        Instant start;
        Instant end;

        if ("ANO".equalsIgnoreCase(opcao)) {
            int targetYear = ano != null ? ano : LocalDate.now(BR_ZONE).getYear();
            start = LocalDate.of(targetYear, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant();
            end = LocalDate.of(targetYear, 12, 31).atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC).toInstant();
        } else if ("MES".equalsIgnoreCase(opcao)) {
            int targetYear = ano != null ? ano : LocalDate.now(BR_ZONE).getYear();
            int targetMonth = mes != null ? mes : LocalDate.now(BR_ZONE).getMonthValue();
            LocalDate firstDay = LocalDate.of(targetYear, targetMonth, 1);
            start = firstDay.atStartOfDay(ZoneOffset.UTC).toInstant();
            end = firstDay.plusMonths(1).minusDays(1).atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC).toInstant();
        } else if ("PERIODO".equalsIgnoreCase(opcao)) {
            start = dataInicio != null ? dataInicio : Instant.EPOCH;
            end = dataFim != null ? dataFim : Instant.now().plusSeconds(31536000); // +1 ano default
        } else {
            throw new IllegalArgumentException("Opção de exportação inválida: " + opcao);
        }

        // 2. Busca os eventos no período com filtros adicionais
        Pageable pageable = PageRequest.of(
                0, 
                100000, 
                Sort.by(Sort.Direction.ASC, "inicioUtc")
        );
        
        List<EventoEntity> eventos = eventRepository.findAllWithFilters(
                start,
                true,
                end,
                true,
                organizacaoIds,
                organizacaoIds != null && !organizacaoIds.isEmpty(),
                null,
                false,
                null,
                false,
                categorias,
                categorias != null && !categorias.isEmpty(),
                null,
                false,
                pageable
        ).getContent();

        // 3. Carrega mapeamentos auxiliares para resolver nomes
        Map<UUID, String> localMap = localRepository.findAll().stream()
                .collect(Collectors.toMap(LocalEntity::getId, LocalEntity::getNome));

        Map<UUID, String> orgMap = organizationRepository.findAll().stream()
                .collect(Collectors.toMap(OrganizacaoEntity::getId, OrganizacaoEntity::getNome));

        // 4. Executa a exportação baseada no formato desejado
        if ("CSV".equalsIgnoreCase(formato)) {
            return gerarCsv(eventos, localMap, orgMap);
        } else if ("EXCEL".equalsIgnoreCase(formato)) {
            return gerarExcel(eventos, localMap, orgMap, start, end);
        } else if ("PDF".equalsIgnoreCase(formato)) {
            return gerarPdf(eventos, localMap, orgMap, start, end);
        } else {
            throw new IllegalArgumentException("Formato de exportação não suportado: " + formato);
        }
    }

    private byte[] gerarCsv(List<EventoEntity> eventos, Map<UUID, String> localMap, Map<UUID, String> orgMap) {
        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM para garantir acentos corretos no Excel em português
        sb.append('\uFEFF');

        // Cabeçalhos
        sb.append("Título;Descrição;Categoria;Início;Fim;Local;Organização Responsável;Status\n");

        // Linhas
        for (EventoEntity e : eventos) {
            sb.append(escapeCsv(e.getTitulo())).append(";")
                    .append(escapeCsv(e.getDescricao())).append(";")
                    .append(escapeCsv(e.getCategoria())).append(";")
                    .append(formatInstant(e.getInicioUtc())).append(";")
                    .append(formatInstant(e.getFimUtc())).append(";")
                    .append(escapeCsv(localMap.getOrDefault(e.getLocalId(), "N/A"))).append(";")
                    .append(escapeCsv(orgMap.getOrDefault(e.getOrganizacaoResponsavelId(), "N/A"))).append(";")
                    .append(escapeCsv(e.getStatus())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] gerarExcel(List<EventoEntity> eventos, Map<UUID, String> localMap, Map<UUID, String> orgMap, Instant start, Instant end) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Calendário");

            // Insere o logo da paróquia na área A1:B4 (Col 0, Linha 0 a Col 1, Linha 3)
            try (java.io.InputStream is = ExportacaoCalendarioUseCase.class.getResourceAsStream("/logo-paroquia.png")) {
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
                    CreationHelper helper = workbook.getCreationHelper();
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setCol1(0);
                    anchor.setRow1(0);
                    anchor.setCol2(2); // Colunas A e B (fim exclusivo)
                    anchor.setRow2(4); // Linhas 0 a 3 (fim exclusivo)
                    drawing.createPicture(anchor, pictureIdx);
                }
            } catch (Exception e) {
                // Silently ignore to guarantee resilience if logo file doesn't exist
            }

            // Título e Subtítulo ao lado do Logo
            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            Row rowTitle = sheet.createRow(1);
            Cell cellTitle = rowTitle.createCell(2); // Coluna C
            cellTitle.setCellValue("Relatório do Calendário de Eventos");
            cellTitle.setCellStyle(titleStyle);

            CellStyle periodStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font periodFont = workbook.createFont();
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
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Cabeçalho na linha 4
            Row headerRow = sheet.createRow(4);
            String[] columns = {"Título", "Descrição", "Categoria", "Início", "Fim", "Local", "Organização Responsável", "Status"};
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
                row.createCell(2).setCellValue(e.getCategoria() != null ? e.getCategoria() : "");
                row.createCell(3).setCellValue(formatInstant(e.getInicioUtc()));
                row.createCell(4).setCellValue(formatInstant(e.getFimUtc()));
                row.createCell(5).setCellValue(localMap.getOrDefault(e.getLocalId(), "N/A"));
                row.createCell(6).setCellValue(orgMap.getOrDefault(e.getOrganizacaoResponsavelId(), "N/A"));
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

    private byte[] gerarPdf(List<EventoEntity> eventos, Map<UUID, String> localMap, Map<UUID, String> orgMap, Instant start, Instant end) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36f, 36f, 60f, 60f); // Margens para cabeçalho/rodapé
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Carrega a logo para cabeçalho e marca d'água
            byte[] logoBytes = null;
            try (java.io.InputStream is = ExportacaoCalendarioUseCase.class.getResourceAsStream("/logo-paroquia.png")) {
                if (is != null) {
                    logoBytes = is.readAllBytes();
                }
            } catch (Exception e) {
                // Silently ignore to keep PDF generation robust even if image is missing
            }

            // Registra o helper de eventos de página
            writer.setPageEvent(new PdfHeaderFooterWatermarkHelper(logoBytes));

            document.open();

            // Título do PDF
            Font fontTitle = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(26, 35, 126));
            Paragraph title = new Paragraph("Relatório do Calendário de Eventos", fontTitle);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(5);
            document.add(title);

            // Subtítulo do período
            Font fontSub = new Font(Font.HELVETICA, 10, Font.NORMAL);
            String periodoStr = "Período: " + formatInstantDateOnly(start) + " a " + formatInstantDateOnly(end);
            Paragraph subtitle = new Paragraph(periodoStr, fontSub);
            subtitle.setAlignment(Paragraph.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            if (eventos == null || eventos.isEmpty()) {
                Font fontInfo = new Font(Font.HELVETICA, 12, Font.NORMAL);
                Paragraph pInfo = new Paragraph("Nenhum evento cadastrado no período selecionado.", fontInfo);
                pInfo.setAlignment(Paragraph.ALIGN_CENTER);
                pInfo.setSpacingBefore(30);
                document.add(pInfo);
            } else {
                // Ordena os eventos cronologicamente por início
                List<EventoEntity> eventosOrdenados = eventos.stream()
                        .sorted((e1, e2) -> e1.getInicioUtc().compareTo(e2.getInicioUtc()))
                        .collect(Collectors.toList());

                String ultimoMesAno = null;
                PdfPTable currentTable = null;

                for (EventoEntity e : eventosOrdenados) {
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(e.getInicioUtc(), BR_ZONE);
                    String mesAno = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM"));

                    if (!mesAno.equals(ultimoMesAno)) {
                        if (ultimoMesAno != null) {
                            document.add(currentTable);
                            document.newPage();
                        }
                        ultimoMesAno = mesAno;

                        // Título do Mês
                        String tituloMes = zdt.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", new Locale("pt", "BR"))).toUpperCase();
                        Font fontMonth = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(26, 35, 126));
                        Paragraph pMonth = new Paragraph(tituloMes, fontMonth);
                        pMonth.setSpacingAfter(8);
                        document.add(pMonth);

                        // Tabela para o mês corrente
                        float[] columnWidths = {3f, 3f, 2f, 2.5f, 2.5f, 2.5f, 3f, 2f};
                        currentTable = new PdfPTable(columnWidths);
                        currentTable.setWidthPercentage(100);
                        currentTable.setSpacingAfter(15);

                        // Estilos do Cabeçalho da Tabela
                        Font fontHeader = new Font(Font.HELVETICA, 9, Font.BOLD);
                        String[] headers = {"Título", "Descrição", "Categoria", "Início", "Fim", "Local", "Organização", "Status"};
                        for (String col : headers) {
                            PdfPCell cellHeader = new PdfPCell(new Phrase(col, fontHeader));
                            cellHeader.setBackgroundColor(new Color(220, 220, 220));
                            cellHeader.setPadding(5);
                            cellHeader.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            currentTable.addCell(cellHeader);
                        }
                    }

                    // Corpo da Tabela
                    Font fontBody = new Font(Font.HELVETICA, 8, Font.NORMAL);
                    currentTable.addCell(new Phrase(e.getTitulo(), fontBody));
                    currentTable.addCell(new Phrase(e.getDescricao() != null ? e.getDescricao() : "", fontBody));
                    currentTable.addCell(new Phrase(e.getCategoria() != null ? e.getCategoria() : "", fontBody));
                    currentTable.addCell(new Phrase(formatInstant(e.getInicioUtc()), fontBody));
                    currentTable.addCell(new Phrase(formatInstant(e.getFimUtc()), fontBody));
                    currentTable.addCell(new Phrase(localMap.getOrDefault(e.getLocalId(), "N/A"), fontBody));
                    currentTable.addCell(new Phrase(orgMap.getOrDefault(e.getOrganizacaoResponsavelId(), "N/A"), fontBody));
                    currentTable.addCell(new Phrase(e.getStatus(), fontBody));
                }

                if (currentTable != null) {
                    document.add(currentTable);
                }
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Erro ao gerar arquivo PDF", e);
        }
    }

    private static class PdfHeaderFooterWatermarkHelper extends PdfPageEventHelper {
        private final Image logoWatermark;
        private final Image logoHeader;
        private final Font headerFont;
        private final Font footerFont;

        public PdfHeaderFooterWatermarkHelper(byte[] logoBytes) {
            Image tempWatermark = null;
            Image logoHeaderImg = null;
            if (logoBytes != null && logoBytes.length > 0) {
                try {
                    tempWatermark = Image.getInstance(logoBytes);
                    tempWatermark.scaleAbsolute(222, 250);
                    tempWatermark.setAbsolutePosition(310f, 172.5f);

                    logoHeaderImg = Image.getInstance(logoBytes);
                    logoHeaderImg.scaleToFit(25, 25);
                } catch (Exception e) {
                    // Ignore
                }
            }
            this.logoWatermark = tempWatermark;
            this.logoHeader = logoHeaderImg;
            this.headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(26, 35, 126)); // Marian Blue
            this.footerFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvasUnder = writer.getDirectContentUnder();
            PdfContentByte canvasOver = writer.getDirectContent();

            // 1. Marca d'água
            if (logoWatermark != null) {
                try {
                    canvasUnder.saveState();
                    PdfGState gstate = new PdfGState();
                    gstate.setFillOpacity(0.08f);
                    gstate.setStrokeOpacity(0.08f);
                    canvasUnder.setGState(gstate);
                    canvasUnder.addImage(logoWatermark);
                    canvasUnder.restoreState();
                } catch (Exception e) {
                    // ignore
                }
            }

            // 2. Cabeçalho
            try {
                PdfPTable headerTable = new PdfPTable(2);
                headerTable.setTotalWidth(770);
                headerTable.setWidths(new float[]{1, 25});

                PdfPCell logoCell;
                if (logoHeader != null) {
                    logoCell = new PdfPCell(logoHeader);
                } else {
                    logoCell = new PdfPCell(new Phrase(""));
                }
                logoCell.setBorder(PdfPCell.BOTTOM);
                logoCell.setBorderColor(Color.LIGHT_GRAY);
                logoCell.setPaddingBottom(5);
                logoCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
                headerTable.addCell(logoCell);

                PdfPCell textCell = new PdfPCell(new Phrase("Paróquia Nossa Senhora de Fátima", headerFont));
                textCell.setBorder(PdfPCell.BOTTOM);
                textCell.setBorderColor(Color.LIGHT_GRAY);
                textCell.setPaddingBottom(5);
                textCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
                textCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                headerTable.addCell(textCell);

                headerTable.writeSelectedRows(0, -1, 36, 565, canvasOver);
            } catch (Exception e) {
                // ignore
            }

            // 3. Rodapé
            try {
                PdfPTable footerTable = new PdfPTable(1);
                footerTable.setTotalWidth(770);

                String footerText = "Paróquia Nossa Senhora de Fátima\nEndereço: Rua Gijoca, 7 - Vila Curuçá Velha - São Paulo/SP, Diocese de São Miguel Paulista";
                PdfPCell footerCell = new PdfPCell(new Phrase(footerText, footerFont));
                footerCell.setBorder(PdfPCell.TOP);
                footerCell.setBorderColor(Color.LIGHT_GRAY);
                footerCell.setPaddingTop(5);
                footerCell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                footerTable.addCell(footerCell);

                footerTable.writeSelectedRows(0, -1, 36, 45, canvasOver);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private String formatInstant(Instant instant) {
        if (instant == null) return "";
        return ZonedDateTime.ofInstant(instant, BR_ZONE).format(DATE_FORMATTER);
    }

    private String formatInstantDateOnly(Instant instant) {
        if (instant == null) return "";
        return ZonedDateTime.ofInstant(instant, BR_ZONE).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private String escapeCsv(Object val) {
        if (val == null) return "";
        String s = val.toString().replace("\"", "\"\"");
        if (s.contains(";") || s.contains("\n") || s.contains("\r") || s.contains("\"")) {
            return "\"" + s + "\"";
        }
        return s;
    }
}
