package br.com.nsfatima.gestao.calendario.infrastructure.export;

import br.com.nsfatima.gestao.calendario.application.export.ExportadorCalendario;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import org.jspecify.annotations.NonNull;
import org.openpdf.text.Chunk;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Exporta uma lista de eventos do calendário para um relatório PDF (via OpenPDF), agrupado por
 * mês, no formato de agenda ilustrada: cada evento vira um cartão com faixa lateral colorida por
 * categoria, título e metadados essenciais — com cabeçalho, rodapé e marca d'água contendo o logo
 * e a identidade visual (cores) da paróquia em cada página.
 */
@Component
public class ExportadorCalendarioPdf implements ExportadorCalendario {

    private static final ZoneId BR_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String NA = "N/A";
    private static final String SEPARADOR = ", ";

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
     * Clareia uma cor misturando-a com branco, usada para gerar o fundo sutil de cada cartão de
     * evento a partir da cor de destaque da categoria.
     *
     * @param color cor base
     * @param fator fração de branco a misturar (0 a 1)
     * @return cor clareada
     */
    private static Color clarear(Color color, float fator) {
        int r = (int) (color.getRed() + (255 - color.getRed()) * fator);
        int g = (int) (color.getGreen() + (255 - color.getGreen()) * fator);
        int b = (int) (color.getBlue() + (255 - color.getBlue()) * fator);
        return new Color(r, g, b);
    }

    /**
     * Indica se este exportador atende ao formato solicitado.
     *
     * @param formato identificador do formato de exportação (ex.: {@code "PDF"})
     * @return {@code true} se o formato, ignorando caixa, for "PDF"
     */
    @Override
    public boolean suporta(String formato) {
        return "PDF".equalsIgnoreCase(formato);
    }

    /**
     * Gera o relatório PDF em formato de agenda: título, período, e um cartão por evento
     * (agrupados por mês, ordenados cronologicamente pelo início), aplicando a identidade visual
     * da paróquia (logo e cor primária) no cabeçalho, rodapé e marca d'água de cada página via
     * {@link PdfHeaderFooterWatermarkPageListener}.
     *
     * @param eventos eventos a serem exportados; se vazio ou {@code null}, o relatório informa
     *     que não há eventos cadastrados no período
     * @param localMap mapa de ID de local para nome de exibição
     * @param orgMap mapa de ID de organização para nome de exibição
     * @param projetoMap mapa de ID de projeto vinculado para nome de exibição
     * @param start início do período exibido no subtítulo do relatório
     * @param end fim do período exibido no subtítulo do relatório
     * @param config configuração da paróquia, usada para logo, cor primária e dados do rodapé
     * @param visualizacao {@code "AGENDA"} (padrão, cartão por evento) ou {@code "GRADE"}
     *     (grade mensal nos moldes da tela do calendário)
     * @return bytes do arquivo PDF gerado
     * @throws RuntimeException se ocorrer erro de geração de documento ou de E/S
     */
    @Override
    public byte[] exportar(List<EventoEntity> eventos,
                           Map<UUID, String> localMap,
                           Map<UUID, String> orgMap,
                           Map<UUID, String> projetoMap,
                           Instant start,
                           Instant end,
                           ParoquiaConfiguracaoEntity config,
                           String visualizacao) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Margens para cabeçalho/rodapé
            Document document = new Document(PageSize.A4.rotate(), 36f, 36f, 60f, 60f);
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
            } else if ("GRADE".equalsIgnoreCase(visualizacao)) {
                adicionarPaginasGrade(document, eventos, primaryColor, localMap);
            } else {
                // Ordena os eventos cronologicamente por início
                List<EventoEntity> eventosOrdenados = eventos.stream()
                        .sorted(Comparator.comparing(EventoEntity::getInicioUtc))
                        .toList();

                String ultimoMesAno = null;

                for (EventoEntity e : eventosOrdenados) {
                    ZonedDateTime zdt = ZonedDateTime.ofInstant(e.getInicioUtc(), BR_ZONE);
                    String mesAno = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM"));

                    if (!mesAno.equals(ultimoMesAno)) {
                        if (ultimoMesAno != null) {
                            document.newPage();
                        }
                        ultimoMesAno = mesAno;

                        String tituloMes = zdt.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy",
                                Locale.of("pt", "BR"))).toUpperCase();

                        Font fontMonth = new Font(Font.HELVETICA, 12, Font.BOLD, primaryColor);
                        Paragraph pMonth = new Paragraph(tituloMes, fontMonth);
                        pMonth.setSpacingAfter(8);
                        document.add(pMonth);
                    }

                    document.add(criarCartaoEvento(e, localMap, orgMap, projetoMap));
                }
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Erro ao gerar arquivo PDF", e);
        }
    }

    /**
     * Adiciona uma página de grade mensal (7 colunas, semanas completas) para cada mês presente
     * nos eventos, na mesma ordem cronológica em que aparecem.
     *
     * @param document documento PDF em construção
     * @param eventos eventos a distribuir na grade
     * @param primaryColor cor primária da paróquia, usada no título do mês
     * @param localMap mapa de ID de local para nome de exibição
     * @throws DocumentException se ocorrer erro ao adicionar elementos ao documento
     */
    private void adicionarPaginasGrade(Document document, List<EventoEntity> eventos, Color primaryColor,
                                       Map<UUID, String> localMap) throws DocumentException {
        Map<String, List<EventoEntity>> porMes = eventos.stream()
                .sorted(Comparator.comparing(EventoEntity::getInicioUtc))
                .collect(Collectors.groupingBy(
                        e -> ZonedDateTime.ofInstant(e.getInicioUtc(), BR_ZONE)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        LinkedHashMap::new,
                        Collectors.toList()));

        boolean primeiraPagina = true;
        for (Map.Entry<String, List<EventoEntity>> entry : porMes.entrySet()) {
            if (!primeiraPagina) {
                document.newPage();
            }
            primeiraPagina = false;

            int ano = Integer.parseInt(entry.getKey().substring(0, 4));
            int mes = Integer.parseInt(entry.getKey().substring(5, 7));
            criarPaginaGradeMes(document, ano, mes, entry.getValue(), primaryColor, localMap);
        }
    }

    /**
     * Monta a grade de um único mês: título, cabeçalho de dias da semana e uma tabela 7 colunas
     * com um dia por célula (número do dia + até {@value #MAX_EVENTOS_CELULA_GRADE} eventos
     * resumidos, com indicador "+N mais" quando houver mais).
     *
     * @param document documento PDF em construção
     * @param ano ano do mês a renderizar
     * @param mes mês a renderizar (1-12)
     * @param eventosDoMes eventos daquele mês
     * @param primaryColor cor primária da paróquia, usada no título do mês
     * @param localMap mapa de ID de local para nome de exibição
     * @throws DocumentException se ocorrer erro ao adicionar elementos ao documento
     */
    private void criarPaginaGradeMes(Document document, int ano, int mes, List<EventoEntity> eventosDoMes,
                                     Color primaryColor, Map<UUID, String> localMap) throws DocumentException {
        LocalDate referencia = LocalDate.of(ano, mes, 1);
        String tituloMes = referencia.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale.of("pt", "BR")))
                .toUpperCase();

        Font fontMonth = new Font(Font.HELVETICA, 12, Font.BOLD, primaryColor);
        Paragraph pMonth = new Paragraph(tituloMes, fontMonth);
        pMonth.setSpacingAfter(8);
        document.add(pMonth);

        Map<LocalDate, List<EventoEntity>> porDia = eventosDoMes.stream()
                .collect(Collectors.groupingBy(e -> ZonedDateTime.ofInstant(e.getInicioUtc(), BR_ZONE).toLocalDate()));

        String[] diasSemana = {"Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado"};
        PdfPTable cabecalho = new PdfPTable(7);
        cabecalho.setWidthPercentage(100);
        Font fontCabecalho = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        for (String dia : diasSemana) {
            PdfPCell cell = new PdfPCell(new Phrase(dia, fontCabecalho));
            cell.setBackgroundColor(primaryColor);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            cell.setPadding(5);
            cabecalho.addCell(cell);
        }
        document.add(cabecalho);

        PdfPTable grade = new PdfPTable(7);
        grade.setWidthPercentage(100);
        for (GradeMensalCalculator.DiaGrade dia : GradeMensalCalculator.calcular(ano, mes)) {
            grade.addCell(criarCelulaGrade(dia, porDia.getOrDefault(dia.data(), List.of()), localMap));
        }
        document.add(grade);
    }

    private static final int MAX_EVENTOS_CELULA_GRADE = 2;

    /**
     * Monta uma célula da grade mensal: número do dia (esmaecido se for de outro mês) seguido
     * dos eventos daquele dia, cada um com uma linha colorida pela categoria.
     *
     * @param dia dia da grade a renderizar
     * @param eventosDoDia eventos alocados naquele dia
     * @param localMap mapa de ID de local para nome de exibição
     * @return célula pronta para compor a tabela da grade
     */
    private PdfPCell criarCelulaGrade(GradeMensalCalculator.DiaGrade dia, List<EventoEntity> eventosDoDia,
                                      Map<UUID, String> localMap) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(4);
        cell.setMinimumHeight(70);
        cell.setVerticalAlignment(PdfPCell.ALIGN_TOP);
        cell.setBackgroundColor(dia.mesAtual() ? Color.WHITE : new Color(0xF5, 0xF5, 0xF5));

        Paragraph conteudo = new Paragraph();
        conteudo.setLeading(11f);

        Font fontDia = new Font(Font.HELVETICA, 9, Font.BOLD, dia.mesAtual() ? Color.DARK_GRAY : Color.LIGHT_GRAY);
        conteudo.add(new Chunk(String.valueOf(dia.data().getDayOfMonth()), fontDia));

        List<EventoEntity> ordenados = eventosDoDia.stream()
                .sorted(Comparator.comparing(EventoEntity::getInicioUtc))
                .toList();

        for (EventoEntity evento : ordenados.stream().limit(MAX_EVENTOS_CELULA_GRADE).toList()) {
            Color corCategoria = CorCategoriaEventoResolver.resolve(evento.getCategoria());
            Font fontEvento = new Font(Font.HELVETICA, 7, Font.NORMAL, corCategoria);
            String titulo = truncar(evento.getTitulo(), 16);
            String local = truncar(localLabel(evento, localMap), 12);
            conteudo.add(Chunk.NEWLINE);
            conteudo.add(new Chunk(
                    "• " + formatInstant(evento.getInicioUtc()) + " " + titulo + " (" + local + ")", fontEvento));
        }

        if (ordenados.size() > MAX_EVENTOS_CELULA_GRADE) {
            Font fontMais = new Font(Font.HELVETICA, 7, Font.ITALIC, Color.GRAY);
            conteudo.add(Chunk.NEWLINE);
            conteudo.add(new Chunk("+" + (ordenados.size() - MAX_EVENTOS_CELULA_GRADE) + " mais", fontMais));
        }

        cell.addElement(conteudo);
        return cell;
    }

    /**
     * Trunca um texto ao tamanho máximo informado, acrescentando reticências quando cortado.
     *
     * @param texto texto a truncar, pode ser {@code null}
     * @param tamanhoMaximo tamanho máximo (sem contar as reticências)
     * @return texto truncado, ou string vazia se {@code texto} for {@code null}
     */
    private String truncar(String texto, int tamanhoMaximo) {
        if (texto == null) {
            return "";
        }
        return texto.length() > tamanhoMaximo ? texto.substring(0, tamanhoMaximo) + "…" : texto;
    }

    /**
     * Monta o cartão visual de um evento: faixa lateral colorida por categoria, título (com
     * indicadores de recorrência/conflito quando aplicável) e uma linha de metadados essenciais.
     *
     * @param e evento a representar
     * @param localMap mapa de ID de local para nome de exibição
     * @param orgMap mapa de ID de organização para nome de exibição
     * @param projetoMap mapa de ID de projeto vinculado para nome de exibição
     * @return tabela de célula única pronta para ser adicionada ao documento
     */
    private PdfPTable criarCartaoEvento(EventoEntity e,
                                        Map<UUID, String> localMap,
                                        Map<UUID, String> orgMap,
                                        Map<UUID, String> projetoMap) {
        Color corCategoria = CorCategoriaEventoResolver.resolve(e.getCategoria());
        Color corFundo = clarear(corCategoria, 0.9f);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingAfter(6);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(corFundo);
        cell.setBorder(PdfPCell.LEFT);
        cell.setBorderWidthLeft(4);
        cell.setBorderColorLeft(corCategoria);
        cell.setPadding(8);

        Font fontTitulo = new Font(Font.HELVETICA, 11, Font.BOLD, Color.DARK_GRAY);
        Font fontBadge = new Font(Font.HELVETICA, 8, Font.BOLD, corCategoria);
        Font fontMeta = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.GRAY);

        Paragraph conteudo = new Paragraph();
        conteudo.setLeading(13f);

        Chunk tituloChunk = new Chunk(e.getTitulo() != null ? e.getTitulo() : "", fontTitulo);
        conteudo.add(tituloChunk);

        if (e.getRecorrenciaId() != null) {
            conteudo.add(new Chunk("  [RECORRENTE]", fontBadge));
        }
        if (e.getEstadoConflito() != null) {
            Font fontConflito = new Font(Font.HELVETICA, 8, Font.BOLD, new Color(0xC6, 0x28, 0x28));
            conteudo.add(new Chunk("  [CONFLITO DE AGENDA]", fontConflito));
        }
        conteudo.add(Chunk.NEWLINE);

        String metaLinha1 = "Categoria: " + (e.getCategoria() != null ? e.getCategoria().name() : NA)
                + "  |  Formato: " + e.getFormato().getDescricao()
                + "  |  Turno: " + turnoLabel(e)
                + "  |  " + formatInstant(e.getInicioUtc()) + " às " + formatInstant(e.getFimUtc());
        conteudo.add(new Chunk(metaLinha1, fontMeta));
        conteudo.add(Chunk.NEWLINE);

        String metaLinha2 = "Local: " + localLabel(e, localMap)
                + "  |  Organização: " + orgMap.getOrDefault(e.getOrganizacaoResponsavelId(), NA)
                + "  |  Status: " + (e.getStatus() != null ? e.getStatus() : NA);
        String nomeProjeto = e.getProjetoId() != null ? projetoMap.get(e.getProjetoId()) : null;
        if (nomeProjeto != null) {
            metaLinha2 += "  |  Projeto: " + nomeProjeto;
        }
        conteudo.add(new Chunk(metaLinha2, fontMeta));

        if (e.getDescricao() != null && !e.getDescricao().isBlank()) {
            conteudo.add(Chunk.NEWLINE);
            conteudo.add(new Chunk(e.getDescricao(), fontMeta));
        }

        Font fontAlerta = new Font(Font.HELVETICA, 9, Font.ITALIC, new Color(0xC6, 0x28, 0x28));
        if (e.getEstadoConflito() != null && e.getMotivoConflito() != null && !e.getMotivoConflito().isBlank()) {
            conteudo.add(Chunk.NEWLINE);
            conteudo.add(new Chunk("Conflito: " + e.getMotivoConflito(), fontAlerta));
        }
        if ("CANCELADO".equalsIgnoreCase(e.getStatus())
                && e.getCanceladoMotivo() != null && !e.getCanceladoMotivo().isBlank()) {
            conteudo.add(Chunk.NEWLINE);
            conteudo.add(new Chunk("Cancelamento: " + e.getCanceladoMotivo(), fontAlerta));
        }
        if (e.getAdicionadoExtraJustificativa() != null && !e.getAdicionadoExtraJustificativa().isBlank()) {
            conteudo.add(Chunk.NEWLINE);
            conteudo.add(new Chunk("Justificativa: " + e.getAdicionadoExtraJustificativa(), fontMeta));
        }

        cell.addElement(conteudo);
        table.addCell(cell);
        return table;
    }

    /**
     * Rótulo amigável do turno/horário do evento: descrição do {@code tipoHorario} predefinido,
     * ou a lista de turnos selecionados quando houver mais de um.
     *
     * @param e evento a descrever
     * @return rótulo pronto para exibição
     */
    private String turnoLabel(EventoEntity e) {
        List<TipoHorarioEvento> periodos = e.getPeriodosList();
        if (periodos.size() > 1) {
            return periodos.stream().map(TipoHorarioEvento::getDescricao).collect(Collectors.joining(SEPARADOR));
        }
        return e.getTipoHorario() != null
                ? e.getTipoHorario().getDescricao()
                : TipoHorarioEvento.PERSONALIZADO.getDescricao();
    }

    /**
     * Rótulo com os nomes de todos os locais alocados ao evento, separados por vírgula.
     *
     * @param e evento a descrever
     * @param localMap mapa de ID de local para nome de exibição
     * @return rótulo pronto para exibição, ou {@code "N/A"} se nenhum local estiver definido
     */
    private String localLabel(EventoEntity e, Map<UUID, String> localMap) {
        List<UUID> ids = e.getLocalIds();
        if (ids.isEmpty()) {
            return NA;
        }
        return ids.stream().map(id -> localMap.getOrDefault(id, NA)).collect(Collectors.joining(SEPARADOR));
    }

    /**
     * Formata um instante UTC como horário no fuso de São Paulo, no padrão {@code HH:mm}.
     *
     * @param instant instante a formatar, pode ser {@code null}
     * @return string formatada, ou vazia se o instante for {@code null}
     */
    private String formatInstant(Instant instant) {
        if (instant == null) {
            return "";
        }
        return ZonedDateTime.ofInstant(instant, BR_ZONE).format(TIME_FORMATTER);
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
        return ZonedDateTime.ofInstant(instant, BR_ZONE).format(DATE_FORMATTER);
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
