package br.com.nsfatima.gestao.calendario.infrastructure.export;

import br.com.nsfatima.gestao.calendario.application.export.CalendarioExporter;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import org.jspecify.annotations.NonNull;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Font;
import org.openpdf.text.Image;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfGState;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Exporta uma lista de eventos do calendário para um relatório PDF (via OpenPDF), agrupado por
 * mês, com cabeçalho, rodapé e marca d'água contendo o logo e a identidade visual (cores) da
 * paróquia em cada página.
 */
@Component
public class PdfCalendarioExporter implements CalendarioExporter {

    private static final ZoneId BR_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Converte uma cor hexadecimal em {@link Color}, retornando uma cor padrão caso o valor seja
     * nulo, vazio ou inválido.
     *
     * @param hex cor em formato hexadecimal (ex.: {@code "#1A237E"}), pode ser {@code null}
     * @param defaultColor cor a ser usada quando {@code hex} for nulo, vazio ou inválido
     * @return a cor decodificada, ou {@code defaultColor} em caso de ausência/erro
     */
    private static Color parseColor(String hex, Color defaultColor) {
        if (hex == null || hex.isBlank()) {
            return defaultColor;
        }
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            return defaultColor;
        }
    }

    /**
     * Indica se este exportador atende ao formato solicitado.
     *
     * @param formato identificador do formato de exportação (ex.: {@code "PDF"})
     * @return {@code true} se o formato, ignorando caixa, for "PDF"
     */
    @Override
    public boolean supports(String formato) {
        return "PDF".equalsIgnoreCase(formato);
    }

    /**
     * Gera o relatório PDF com título, período, e uma tabela de eventos por mês (ordenados
     * cronologicamente pelo início), aplicando a identidade visual da paróquia (logo e cor
     * primária) no cabeçalho, rodapé e marca d'água de cada página via
     * {@link PdfHeaderFooterWatermarkPageListener}.
     *
     * @param eventos eventos a serem exportados; se vazio ou {@code null}, o relatório informa
     *     que não há eventos cadastrados no período
     * @param localMap mapa de ID de local para nome de exibição
     * @param orgMap mapa de ID de organização para nome de exibição
     * @param start início do período exibido no subtítulo do relatório
     * @param end fim do período exibido no subtítulo do relatório
     * @param config configuração da paróquia, usada para logo, cor primária e dados do rodapé
     * @return bytes do arquivo PDF gerado
     * @throws RuntimeException se ocorrer erro de geração de documento ou de E/S
     */
    @Override
    public byte[] export(List<EventoEntity> eventos,
                         Map<UUID, String> localMap,
                         Map<UUID, String> orgMap,
                         Instant start,
                         Instant end,
                         ParoquiaConfiguracaoEntity config) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36f, 36f, 60f, 60f); // Margens para cabeçalho/rodapé
            PdfWriter writer = PdfWriter.getInstance(document, out);

            // Registra o helper de eventos de página
            writer.setPageEvent(new PdfHeaderFooterWatermarkPageListener(config));

            document.open();

            // Título do PDF com cor primária dinâmica
            Color primaryColor = parseColor(config != null ? config.getCorPrimariaHex() : null, new Color(26, 35, 126));
            Font fontTitle = new Font(Font.HELVETICA, 18, Font.BOLD, primaryColor);
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
                        .sorted(Comparator.comparing(EventoEntity::getInicioUtc))
                        .toList();

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
                        String tituloMes = zdt.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy",
                                Locale.of("pt", "BR"))).toUpperCase();

                        Font fontMonth = new Font(Font.HELVETICA, 12, Font.BOLD, primaryColor);
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
                        String[] headers = {"Título",
                                "Descrição", "Categoria", "Início", "Fim", "Local", "Organização", "Status"};

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
                    currentTable.addCell(new Phrase(e.getCategoria() != null ? e.getCategoria().name() : "", fontBody));
                    currentTable.addCell(new Phrase(formatInstant(e.getInicioUtc()), fontBody));
                    currentTable.addCell(new Phrase(formatInstant(e.getFimUtc()), fontBody));
                    currentTable.addCell(new Phrase(localMap.getOrDefault(e.getLocalId(), "N/A"), fontBody));
                    currentTable.addCell(new Phrase(orgMap.getOrDefault(e.getOrganizacaoResponsavelId(),
                            "N/A"), fontBody));

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

    /**
     * Listener de eventos de página do OpenPDF responsável por desenhar, em cada página do
     * relatório, a marca d'água com o logo da paróquia (com baixa opacidade), o cabeçalho com
     * logo e nome da paróquia, e o rodapé com endereço e diocese.
     */
    private static class PdfHeaderFooterWatermarkPageListener extends PdfPageEventHelper {
        private final Image logoWatermark;
        private final Image logoHeader;
        private final Font headerFont;
        private final Font footerFont;
        private final String paroquiaNome;
        private final String footerText;

        /**
         * Prepara as imagens (marca d'água e logo de cabeçalho), fontes e textos de rodapé a
         * partir da configuração da paróquia, aplicando valores padrão quando a configuração ou
         * o logo não estiverem disponíveis.
         *
         * @param config configuração da paróquia usada para obter logo, cores, nome, diocese e
         *     endereço; pode ser {@code null}, caso em que valores padrão são usados
         */
        public PdfHeaderFooterWatermarkPageListener(ParoquiaConfiguracaoEntity config) {
            Image tempWatermark = null;
            Image logoHeaderImg = null;
            byte[] logoBytes = config != null ? config.getLogoBytes() : null;

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

            String nome = config != null ? config.getNome() : "Paróquia Nossa Senhora de Fátima";
            String diocese = config != null ? config.getDiocese() : "Diocese de São Miguel Paulista";
            String endereco =
                    config != null ? config.getEndereco() : "Rua Gijoca, 7 - Vila Curuçá Velha - São Paulo/SP";
            this.paroquiaNome = nome;
            this.footerText = nome + "\nEndereço: " + endereco + ", " + diocese;

            Color primaryColor = parseColor(config != null ? config.getCorPrimariaHex() : null, new Color(26, 35, 126));
            this.headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, primaryColor);
            this.footerFont = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);
        }

        /**
         * Desenha a marca d'água, o cabeçalho e o rodapé ao final da renderização de cada
         * página do documento. Falhas em qualquer uma das três etapas são ignoradas
         * individualmente para não interromper a geração do restante do relatório.
         *
         * @param writer escritor PDF usado para obter os canvases de desenho
         * @param document documento sendo gerado
         */
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
                PdfPTable headerTable = createHeaderTable();

                headerTable.writeSelectedRows(0, -1, 36, 565, canvasOver);
            } catch (Exception e) {
                // ignore
            }

            // 3. Rodapé
            try {
                PdfPTable footerTable = new PdfPTable(1);
                footerTable.setTotalWidth(770);

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

        /**
         * Monta a tabela de duas colunas (logo e nome da paróquia) usada como cabeçalho de
         * página, com borda inferior separando-a do conteúdo.
         *
         * @return tabela de cabeçalho pronta para ser escrita no topo da página
         */
        private @NonNull PdfPTable createHeaderTable() {
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setTotalWidth(770);
            headerTable.setWidths(new float[] {1, 25});

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

            PdfPCell textCell = new PdfPCell(new Phrase(paroquiaNome, headerFont));
            textCell.setBorder(PdfPCell.BOTTOM);
            textCell.setBorderColor(Color.LIGHT_GRAY);
            textCell.setPaddingBottom(5);
            textCell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            textCell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
            headerTable.addCell(textCell);
            return headerTable;
        }
    }
}
