package br.com.nsfatima.gestao.calendario.infrastructure.export;

import br.com.nsfatima.gestao.calendario.application.export.ExportadorCalendario;
import br.com.nsfatima.gestao.calendario.domain.model.VarianteFundoLogo;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.ShapeType;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.Rectangle;
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
 * Exporta uma lista de eventos do calendário para uma apresentação PowerPoint (.pptx) usando
 * Apache POI (XSLF), no formato de agenda ilustrada: um slide de capa com a identidade visual da
 * paróquia, seguido de um slide por mês com os eventos daquele mês representados como cartões
 * coloridos por categoria — mesma linguagem visual do relatório em PDF.
 */
@Component
public class ExportadorCalendarioPptx implements ExportadorCalendario {

    private static final ZoneId BR_ZONE = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final double SLIDE_WIDTH = 960;
    private static final double SLIDE_HEIGHT = 540;
    private static final double MARGIN = 40;
    private static final double CARD_HEIGHT = 78;
    private static final double CARD_GAP = 10;
    private static final String NA = "N/A";
    private static final String SEPARADOR = ", ";
    private static final String META_SEPARADOR = "  |  ";

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

    @Override
    public boolean suporta(String formato) {
        return "POWERPOINT".equalsIgnoreCase(formato) || "PPTX".equalsIgnoreCase(formato);
    }

    /**
     * Gera a apresentação com um slide de capa e um slide por mês com eventos, no formato de
     * cartões coloridos por categoria.
     *
     * @param eventos eventos a serem exportados
     * @param localMap mapa de ID de local para nome de exibição
     * @param orgMap mapa de ID de organização para nome de exibição
     * @param projetoMap mapa de ID de projeto vinculado para nome de exibição
     * @param start início do período exibido na capa
     * @param end fim do período exibido na capa
     * @param config configuração da paróquia, usada para logo, cor primária e nome
     * @param visualizacao {@code "AGENDA"} (padrão, cartão por evento) ou {@code "GRADE"}
     *     (grade mensal nos moldes da tela do calendário)
     * @return bytes do arquivo .pptx gerado
     * @throws RuntimeException se ocorrer erro de E/S na serialização da apresentação
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
        try (XMLSlideShow slideShow = new XMLSlideShow(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            slideShow.setPageSize(new java.awt.Dimension((int) SLIDE_WIDTH, (int) SLIDE_HEIGHT));

            Color primaryColor = parseColor(config != null ? config.getCorPrimariaHex() : null, new Color(26, 35, 126));
            String nomeParoquia = config != null && config.getNome() != null
                    ? config.getNome() : "Paróquia Nossa Senhora de Fátima";

            criarSlideCapa(slideShow, config, primaryColor, nomeParoquia, start, end);

            if (eventos != null && !eventos.isEmpty()) {
                Map<String, List<EventoEntity>> porMes = eventos.stream()
                        .sorted(Comparator.comparing(EventoEntity::getInicioUtc))
                        .collect(Collectors.groupingBy(
                                e -> ZonedDateTime.ofInstant(e.getInicioUtc(), BR_ZONE)
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM")),
                                LinkedHashMap::new,
                                Collectors.toList()));

                boolean modoGrade = "GRADE".equalsIgnoreCase(visualizacao);
                for (Map.Entry<String, List<EventoEntity>> entry : porMes.entrySet()) {
                    if (modoGrade) {
                        int ano = Integer.parseInt(entry.getKey().substring(0, 4));
                        int mes = Integer.parseInt(entry.getKey().substring(5, 7));
                        criarSlideGradeMes(slideShow, ano, mes, entry.getValue(), primaryColor, localMap);
                    } else {
                        criarSlidesMes(slideShow, entry.getValue(), primaryColor, localMap, orgMap, projetoMap);
                    }
                }
            }

            slideShow.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar arquivo PowerPoint", e);
        }
    }

    private void criarSlideCapa(XMLSlideShow slideShow, ParoquiaConfiguracaoEntity config, Color primaryColor,
                                String nomeParoquia, Instant start, Instant end) {
        XSLFSlide slide = slideShow.createSlide();

        XSLFAutoShape faixa = slide.createAutoShape();
        faixa.setShapeType(ShapeType.RECT);
        faixa.setAnchor(new Rectangle(0, 0, (int) SLIDE_WIDTH, 260));
        faixa.setFillColor(primaryColor);
        faixa.setLineColor(null);

        byte[] logoBytes = config != null ? config.getLogoBytesPorVariante(VarianteFundoLogo.ESCURO) : null;
        if (logoBytes != null && logoBytes.length > 0) {
            try {
                XSLFPictureData pictureData = slideShow.addPicture(logoBytes, PictureData.PictureType.PNG);
                slide.createPicture(pictureData).setAnchor(new Rectangle((int) (SLIDE_WIDTH / 2 - 45), 30, 90, 90));
            } catch (Exception ex) {
                // ignore falha ao carregar logo
            }
        }

        XSLFTextBox tituloBox = slide.createTextBox();
        tituloBox.setAnchor(new Rectangle(60, 130, (int) SLIDE_WIDTH - 120, 60));
        XSLFTextParagraph tituloParagraph = tituloBox.addNewTextParagraph();
        tituloParagraph.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun tituloRun = tituloParagraph.addNewTextRun();
        tituloRun.setText("Calendário de Eventos");
        tituloRun.setFontSize(32d);
        tituloRun.setBold(true);
        tituloRun.setFontColor(Color.WHITE);

        XSLFTextBox subtituloBox = slide.createTextBox();
        subtituloBox.setAnchor(new Rectangle(60, 190, (int) SLIDE_WIDTH - 120, 40));
        XSLFTextParagraph subParagraph = subtituloBox.addNewTextParagraph();
        subParagraph.setTextAlign(TextParagraph.TextAlign.CENTER);
        XSLFTextRun subRun = subParagraph.addNewTextRun();
        subRun.setText(nomeParoquia + "  •  Período: " + formatDate(start) + " a " + formatDate(end));
        subRun.setFontSize(16d);
        subRun.setFontColor(Color.WHITE);
    }

    private void criarSlidesMes(XMLSlideShow slideShow, List<EventoEntity> eventosDoMes, Color primaryColor,
                                Map<UUID, String> localMap, Map<UUID, String> orgMap, Map<UUID, String> projetoMap) {
        ZonedDateTime primeiraData = ZonedDateTime.ofInstant(eventosDoMes.getFirst().getInicioUtc(), BR_ZONE);
        String tituloMes = primeiraData
                .format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale.of("pt", "BR")))
                .toUpperCase();

        double topoConteudo = MARGIN + 55;
        double limiteInferior = SLIDE_HEIGHT - MARGIN;

        XSLFSlide slide = criarSlideMesComTitulo(slideShow, tituloMes, primaryColor);
        double y = topoConteudo;

        for (EventoEntity evento : eventosDoMes) {
            double altura = calcularAlturaCartao(evento);
            if (y + altura > limiteInferior && y > topoConteudo) {
                slide = criarSlideMesComTitulo(slideShow, tituloMes, primaryColor);
                y = topoConteudo;
            }
            criarCartaoEvento(slide, evento, y, altura, localMap, orgMap, projetoMap);
            y += altura + CARD_GAP;
        }
    }

    private XSLFSlide criarSlideMesComTitulo(XMLSlideShow slideShow, String tituloMes, Color primaryColor) {
        XSLFSlide slide = slideShow.createSlide();

        XSLFTextBox tituloBox = slide.createTextBox();
        tituloBox.setAnchor(new Rectangle((int) MARGIN, 15, (int) (SLIDE_WIDTH - 2 * MARGIN), 35));
        XSLFTextParagraph tituloParagraph = tituloBox.addNewTextParagraph();
        XSLFTextRun tituloRun = tituloParagraph.addNewTextRun();
        tituloRun.setText(tituloMes);
        tituloRun.setFontSize(20d);
        tituloRun.setBold(true);
        tituloRun.setFontColor(primaryColor);
        return slide;
    }

    /**
     * Estima a altura necessária para o cartão de um evento, somando uma linha extra para cada
     * informação opcional presente (descrição, conflito, cancelamento, justificativa).
     *
     * @param e evento a medir
     * @return altura em pontos para o cartão
     */
    private double calcularAlturaCartao(EventoEntity e) {
        int linhasExtras = 0;
        if (e.getDescricao() != null && !e.getDescricao().isBlank()) {
            linhasExtras++;
        }
        if (e.getEstadoConflito() != null && e.getMotivoConflito() != null && !e.getMotivoConflito().isBlank()) {
            linhasExtras++;
        }
        if ("CANCELADO".equalsIgnoreCase(e.getStatus())
                && e.getCanceladoMotivo() != null && !e.getCanceladoMotivo().isBlank()) {
            linhasExtras++;
        }
        if (e.getAdicionadoExtraJustificativa() != null && !e.getAdicionadoExtraJustificativa().isBlank()) {
            linhasExtras++;
        }
        return CARD_HEIGHT + linhasExtras * 14;
    }

    private void criarCartaoEvento(XSLFSlide slide, EventoEntity e, double y, double altura,
                                   Map<UUID, String> localMap, Map<UUID, String> orgMap, Map<UUID, String> projetoMap) {
        Color corCategoria = CorCategoriaEventoResolver.resolve(e.getCategoria());
        Color corFundo = clarear(corCategoria, 0.92f);
        double largura = SLIDE_WIDTH - 2 * MARGIN;

        XSLFAutoShape fundo = slide.createAutoShape();
        fundo.setShapeType(ShapeType.RECT);
        fundo.setAnchor(new Rectangle((int) MARGIN, (int) y, (int) largura, (int) altura));
        fundo.setFillColor(corFundo);
        fundo.setLineColor(null);

        XSLFAutoShape faixa = slide.createAutoShape();
        faixa.setShapeType(ShapeType.RECT);
        faixa.setAnchor(new Rectangle((int) MARGIN, (int) y, 6, (int) altura));
        faixa.setFillColor(corCategoria);
        faixa.setLineColor(null);

        XSLFTextBox textBox = slide.createTextBox();
        textBox.setAnchor(new Rectangle((int) MARGIN + 16, (int) y + 6, (int) largura - 32, (int) altura - 12));

        XSLFTextParagraph tituloParagraph = textBox.addNewTextParagraph();
        XSLFTextRun tituloRun = tituloParagraph.addNewTextRun();
        String badges = (e.getRecorrenciaId() != null ? "  [RECORRENTE]" : "")
                + (e.getEstadoConflito() != null ? "  [CONFLITO]" : "");
        tituloRun.setText((e.getTitulo() != null ? e.getTitulo() : "") + badges);
        tituloRun.setFontSize(14d);
        tituloRun.setBold(true);
        tituloRun.setFontColor(Color.DARK_GRAY);

        XSLFTextParagraph metaParagraph = textBox.addNewTextParagraph();
        XSLFTextRun metaRun = metaParagraph.addNewTextRun();
        String nomeProjeto = e.getProjetoId() != null ? projetoMap.get(e.getProjetoId()) : null;
        String metaTexto = "Categoria: " + (e.getCategoria() != null ? e.getCategoria().name() : NA)
                + META_SEPARADOR + "Status: " + (e.getStatus() != null ? e.getStatus() : NA)
                + META_SEPARADOR + formatTime(e.getInicioUtc()) + " às " + formatTime(e.getFimUtc())
                + META_SEPARADOR + e.getFormato().getDescricao()
                + META_SEPARADOR + turnoLabel(e)
                + META_SEPARADOR + localLabel(e, localMap)
                + META_SEPARADOR + orgMap.getOrDefault(e.getOrganizacaoResponsavelId(), NA)
                + (nomeProjeto != null ? META_SEPARADOR + "Projeto: " + nomeProjeto : "");
        metaRun.setText(metaTexto);
        metaRun.setFontSize(10d);
        metaRun.setFontColor(Color.GRAY);

        if (e.getDescricao() != null && !e.getDescricao().isBlank()) {
            XSLFTextRun descRun = textBox.addNewTextParagraph().addNewTextRun();
            descRun.setText(e.getDescricao());
            descRun.setFontSize(9d);
            descRun.setItalic(true);
            descRun.setFontColor(Color.GRAY);
        }

        Color corAlerta = new Color(0xC6, 0x28, 0x28);
        if (e.getEstadoConflito() != null && e.getMotivoConflito() != null && !e.getMotivoConflito().isBlank()) {
            XSLFTextRun run = textBox.addNewTextParagraph().addNewTextRun();
            run.setText("Conflito: " + e.getMotivoConflito());
            run.setFontSize(9d);
            run.setItalic(true);
            run.setFontColor(corAlerta);
        }
        if ("CANCELADO".equalsIgnoreCase(e.getStatus())
                && e.getCanceladoMotivo() != null && !e.getCanceladoMotivo().isBlank()) {
            XSLFTextRun run = textBox.addNewTextParagraph().addNewTextRun();
            run.setText("Cancelamento: " + e.getCanceladoMotivo());
            run.setFontSize(9d);
            run.setItalic(true);
            run.setFontColor(corAlerta);
        }
        if (e.getAdicionadoExtraJustificativa() != null && !e.getAdicionadoExtraJustificativa().isBlank()) {
            XSLFTextRun run = textBox.addNewTextParagraph().addNewTextRun();
            run.setText("Justificativa: " + e.getAdicionadoExtraJustificativa());
            run.setFontSize(9d);
            run.setItalic(true);
            run.setFontColor(Color.GRAY);
        }
    }

    private static final int MAX_EVENTOS_CELULA_GRADE = 2;
    private static final String[] DIAS_SEMANA = {"Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado"};

    /**
     * Monta o slide da grade mensal (7 colunas, semanas completas): título do mês, cabeçalho de
     * dias da semana e uma célula por dia com número + até {@value #MAX_EVENTOS_CELULA_GRADE}
     * eventos resumidos, coloridos por categoria.
     *
     * @param slideShow apresentação em construção
     * @param ano ano do mês a renderizar
     * @param mes mês a renderizar (1-12)
     * @param eventosDoMes eventos daquele mês
     * @param primaryColor cor primária da paróquia, usada no título
     * @param localMap mapa de ID de local para nome de exibição
     */
    private void criarSlideGradeMes(XMLSlideShow slideShow, int ano, int mes, List<EventoEntity> eventosDoMes,
                                    Color primaryColor, Map<UUID, String> localMap) {
        LocalDate referencia = LocalDate.of(ano, mes, 1);
        String tituloMes = referencia.format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale.of("pt", "BR")))
                .toUpperCase();

        XSLFSlide slide = criarSlideMesComTitulo(slideShow, tituloMes, primaryColor);

        Map<LocalDate, List<EventoEntity>> porDia = eventosDoMes.stream()
                .collect(Collectors.groupingBy(e -> ZonedDateTime.ofInstant(e.getInicioUtc(), BR_ZONE).toLocalDate()));

        double areaLargura = SLIDE_WIDTH - 2 * MARGIN;
        double colunaLargura = areaLargura / 7;
        double topoCabecalho = MARGIN + 45;
        double alturaCabecalho = 22;

        for (int i = 0; i < DIAS_SEMANA.length; i++) {
            XSLFAutoShape cabecalho = slide.createAutoShape();
            cabecalho.setShapeType(ShapeType.RECT);
            cabecalho.setAnchor(new Rectangle(
                    (int) (MARGIN + i * colunaLargura), (int) topoCabecalho,
                    (int) colunaLargura, (int) alturaCabecalho));
            cabecalho.setFillColor(primaryColor);
            cabecalho.setLineColor(Color.WHITE);
            XSLFTextParagraph paragraph = cabecalho.addNewTextParagraph();
            paragraph.setTextAlign(TextParagraph.TextAlign.CENTER);
            XSLFTextRun run = paragraph.addNewTextRun();
            run.setText(DIAS_SEMANA[i]);
            run.setFontSize(9d);
            run.setBold(true);
            run.setFontColor(Color.WHITE);
        }

        List<GradeMensalCalculator.DiaGrade> dias = GradeMensalCalculator.calcular(ano, mes);
        int numeroSemanas = dias.size() / 7;
        double topoGrade = topoCabecalho + alturaCabecalho;
        double alturaLinha = (SLIDE_HEIGHT - MARGIN - topoGrade) / numeroSemanas;

        for (int indice = 0; indice < dias.size(); indice++) {
            GradeMensalCalculator.DiaGrade dia = dias.get(indice);
            int linha = indice / 7;
            int coluna = indice % 7;
            double x = MARGIN + coluna * colunaLargura;
            double y = topoGrade + linha * alturaLinha;
            criarCelulaGrade(slide, dia, porDia.getOrDefault(dia.data(), List.of()),
                    x, y, colunaLargura, alturaLinha, localMap);
        }
    }

    private void criarCelulaGrade(XSLFSlide slide, GradeMensalCalculator.DiaGrade dia, List<EventoEntity> eventosDoDia,
                                  double x, double y, double largura, double altura, Map<UUID, String> localMap) {
        XSLFAutoShape fundo = slide.createAutoShape();
        fundo.setShapeType(ShapeType.RECT);
        fundo.setAnchor(new Rectangle((int) x, (int) y, (int) largura, (int) altura));
        fundo.setFillColor(dia.mesAtual() ? Color.WHITE : new Color(0xF5, 0xF5, 0xF5));
        fundo.setLineColor(Color.LIGHT_GRAY);

        XSLFTextBox textBox = slide.createTextBox();
        textBox.setAnchor(new Rectangle((int) x + 3, (int) y + 2, (int) largura - 6, (int) altura - 4));

        XSLFTextParagraph diaParagraph = textBox.addNewTextParagraph();
        XSLFTextRun diaRun = diaParagraph.addNewTextRun();
        diaRun.setText(String.valueOf(dia.data().getDayOfMonth()));
        diaRun.setFontSize(9d);
        diaRun.setBold(true);
        diaRun.setFontColor(dia.mesAtual() ? Color.DARK_GRAY : Color.LIGHT_GRAY);

        List<EventoEntity> ordenados = eventosDoDia.stream()
                .sorted(Comparator.comparing(EventoEntity::getInicioUtc))
                .toList();

        for (EventoEntity evento : ordenados.stream().limit(MAX_EVENTOS_CELULA_GRADE).toList()) {
            Color corCategoria = CorCategoriaEventoResolver.resolve(evento.getCategoria());
            XSLFTextRun run = textBox.addNewTextParagraph().addNewTextRun();
            String titulo = truncar(evento.getTitulo(), 14);
            String local = truncar(localLabel(evento, localMap), 10);
            run.setText(formatTime(evento.getInicioUtc()) + " " + titulo + " (" + local + ")");
            run.setFontSize(6.5d);
            run.setFontColor(corCategoria);
        }

        if (ordenados.size() > MAX_EVENTOS_CELULA_GRADE) {
            XSLFTextRun run = textBox.addNewTextParagraph().addNewTextRun();
            run.setText("+" + (ordenados.size() - MAX_EVENTOS_CELULA_GRADE) + " mais");
            run.setFontSize(6.5d);
            run.setItalic(true);
            run.setFontColor(Color.GRAY);
        }
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

    private static Color clarear(Color color, float fator) {
        int r = (int) (color.getRed() + (255 - color.getRed()) * fator);
        int g = (int) (color.getGreen() + (255 - color.getGreen()) * fator);
        int b = (int) (color.getBlue() + (255 - color.getBlue()) * fator);
        return new Color(r, g, b);
    }

    private String turnoLabel(EventoEntity e) {
        List<TipoHorarioEvento> periodos = e.getPeriodosList();
        if (periodos.size() > 1) {
            return periodos.stream().map(TipoHorarioEvento::getDescricao).collect(Collectors.joining(SEPARADOR));
        }
        return e.getTipoHorario() != null
                ? e.getTipoHorario().getDescricao()
                : TipoHorarioEvento.PERSONALIZADO.getDescricao();
    }

    private String localLabel(EventoEntity e, Map<UUID, String> localMap) {
        List<UUID> ids = e.getLocalIds();
        if (ids.isEmpty()) {
            return NA;
        }
        return ids.stream().map(id -> localMap.getOrDefault(id, NA)).collect(Collectors.joining(SEPARADOR));
    }

    private String formatTime(Instant instant) {
        if (instant == null) {
            return "";
        }
        return ZonedDateTime.ofInstant(instant, BR_ZONE).format(TIME_FORMATTER);
    }

    private String formatDate(Instant instant) {
        if (instant == null) {
            return "";
        }
        return ZonedDateTime.ofInstant(instant, BR_ZONE).format(DATE_FORMATTER);
    }
}
