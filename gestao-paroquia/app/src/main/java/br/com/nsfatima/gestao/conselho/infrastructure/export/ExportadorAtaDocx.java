package br.com.nsfatima.gestao.conselho.infrastructure.export;

import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Assinaturas;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Destaque;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Paragrafo;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.QuebraDePagina;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Tabela;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Titulo;
import br.com.nsfatima.gestao.conselho.application.dto.CabecalhoParoquia;
import br.com.nsfatima.gestao.conselho.application.dto.DocumentoReuniao;
import br.com.nsfatima.gestao.conselho.application.dto.FormatoExportacaoAta;
import br.com.nsfatima.gestao.conselho.application.port.out.ExportadorAta;
import org.apache.poi.common.usermodel.PictureType;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Desenha o documento da ata em Word (.docx, via POI XWPF). Recebe os mesmos blocos neutros do PDF, então o
 * texto é idêntico nos dois formatos. O cabeçalho institucional e a marca de minuta ficam no cabeçalho do
 * documento, para se repetirem em todas as páginas.
 */
@Component
public class ExportadorAtaDocx implements ExportadorAta {

    private static final String FONTE = "Arial";
    private static final String AUTO = "auto";
    private static final String COR_CABECALHO_TABELA = "EEEEEE";
    private static final String COR_DESTAQUE = "B02020";
    private static final String LINHA_ASSINATURA = "______________________________";
    private static final int ALTURA_LOGO_PONTOS = 40;

    @Override
    public boolean suporta(FormatoExportacaoAta formato) {
        return formato == FormatoExportacaoAta.DOCX;
    }

    @Override
    public byte[] exportar(DocumentoReuniao documento) {
        try (XWPFDocument docx = new XWPFDocument(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            docx.getProperties().getCoreProperties().setTitle(documento.titulo());
            cabecalho(docx, documento);
            rodape(docx, documento.emissao());
            for (BlocoDocumento bloco : documento.blocos()) {
                desenhar(docx, bloco);
            }
            docx.write(saida);
            return saida.toByteArray();
        } catch (IOException falha) {
            throw new IllegalStateException("Falha ao gerar o Word da ata.", falha);
        }
    }

    private static void cabecalho(XWPFDocument docx, DocumentoReuniao documento) {
        XWPFHeader cabecalho = docx.createHeader(HeaderFooterType.DEFAULT);
        CabecalhoParoquia c = documento.cabecalho();
        XWPFParagraph identidade = cabecalho.createParagraph();
        XWPFRun nome = identidade.createRun();
        logo(identidade, c);
        texto(nome, c.nome(), 12, true, null);
        nome.addBreak();
        texto(identidade.createRun(), c.diocese() + " — " + c.endereco(), 8, false, "606060");
        if (documento.minuta()) {
            XWPFParagraph aviso = cabecalho.createParagraph();
            aviso.setAlignment(ParagraphAlignment.CENTER);
            texto(aviso.createRun(), "MINUTA — SEM EFEITO", 14, true, COR_DESTAQUE);
        }
    }

    private static void logo(XWPFParagraph paragrafo, CabecalhoParoquia c) {
        PictureType tipo = tipoDaImagem(c.tipoMidiaLogo());
        if (c.logo() == null || c.logo().length == 0 || tipo == null) {
            return;
        }
        try {
            BufferedImage imagem = ImageIO.read(new ByteArrayInputStream(c.logo()));
            if (imagem == null) {
                return;
            }
            int largura = ALTURA_LOGO_PONTOS * imagem.getWidth() / imagem.getHeight();
            paragrafo.createRun().addPicture(new ByteArrayInputStream(c.logo()), tipo, "logo",
                    Units.toEMU(largura), Units.toEMU(ALTURA_LOGO_PONTOS));
        } catch (IOException | InvalidFormatException semLogo) {
            return;
        }
    }

    private static PictureType tipoDaImagem(String tipoMidia) {
        if ("image/png".equals(tipoMidia)) {
            return PictureType.PNG;
        }
        return "image/jpeg".equals(tipoMidia) ? PictureType.JPEG : null;
    }

    private static void rodape(XWPFDocument docx, String emissao) {
        XWPFFooter rodape = docx.createFooter(HeaderFooterType.DEFAULT);
        XWPFParagraph paragrafo = rodape.createParagraph();
        texto(paragrafo.createRun(), emissao, 8, false, "606060");
    }

    private static void desenhar(XWPFDocument docx, BlocoDocumento bloco) {
        switch (bloco) {
            case Titulo t -> titulo(docx, t);
            case Paragrafo p -> paragrafo(docx, p.texto(), false, null, ParagraphAlignment.BOTH);
            case Destaque d -> paragrafo(docx, d.texto(), true, COR_DESTAQUE, ParagraphAlignment.CENTER);
            case Tabela t -> tabela(docx, t);
            case Assinaturas a -> assinaturas(docx, a.rotulos());
            case QuebraDePagina _ -> docx.createParagraph().createRun().addBreak(BreakType.PAGE);
        }
    }

    private static void titulo(XWPFDocument docx, Titulo t) {
        XWPFParagraph paragrafo = docx.createParagraph();
        paragrafo.setAlignment(t.nivel() == 1 ? ParagraphAlignment.CENTER : ParagraphAlignment.LEFT);
        paragrafo.setSpacingBefore(t.nivel() == 1 ? 80 : 200);
        paragrafo.setSpacingAfter(120);
        int tamanho = t.nivel() == 1 ? 15 : t.nivel() == 2 ? 13 : 11;
        texto(paragrafo.createRun(), t.texto(), tamanho, true, null);
    }

    private static void paragrafo(XWPFDocument docx, String texto, boolean negrito, String cor,
                                  ParagraphAlignment alinhamento) {
        XWPFParagraph paragrafo = docx.createParagraph();
        paragrafo.setAlignment(alinhamento);
        paragrafo.setSpacingAfter(120);
        texto(paragrafo.createRun(), texto, 10, negrito, cor);
    }

    private static void tabela(XWPFDocument docx, Tabela t) {
        int colunas = t.cabecalho().isEmpty() ? t.linhas().get(0).size() : t.cabecalho().size();
        XWPFTable tabela = docx.createTable();
        tabela.setWidth("100%");
        boolean comCabecalho = !t.cabecalho().isEmpty();
        if (comCabecalho) {
            linha(tabela.getRow(0), t.cabecalho(), colunas, true, false);
        }
        for (int i = 0; i < t.linhas().size(); i++) {
            XWPFTableRow linha = comCabecalho || i > 0 ? tabela.createRow() : tabela.getRow(0);
            linha(linha, t.linhas().get(i), colunas, false, !comCabecalho);
        }
        docx.createParagraph().setSpacingAfter(80);
    }

    private static void linha(XWPFTableRow linha, List<String> valores, int colunas, boolean cabecalho,
                              boolean primeiraColunaEmRotulo) {
        while (linha.getTableCells().size() < colunas) {
            linha.addNewTableCell();
        }
        for (int i = 0; i < valores.size(); i++) {
            XWPFTableCell celula = linha.getCell(i);
            boolean destaque = cabecalho || (primeiraColunaEmRotulo && i == 0);
            if (destaque) {
                celula.setColor(COR_CABECALHO_TABELA);
            }
            texto(celula.getParagraphs().get(0).createRun(), valores.get(i), 10, destaque, null);
        }
    }

    private static void assinaturas(XWPFDocument docx, List<String> rotulos) {
        XWPFTable tabela = docx.createTable(1, rotulos.size());
        tabela.setWidth("100%");
        tabela.setTopBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, AUTO);
        tabela.setBottomBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, AUTO);
        tabela.setLeftBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, AUTO);
        tabela.setRightBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, AUTO);
        tabela.setInsideVBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, AUTO);
        for (int i = 0; i < rotulos.size(); i++) {
            XWPFParagraph paragrafo = tabela.getRow(0).getCell(i).getParagraphs().get(0);
            paragrafo.setAlignment(ParagraphAlignment.CENTER);
            paragrafo.setSpacingBefore(720);
            XWPFRun run = paragrafo.createRun();
            texto(run, LINHA_ASSINATURA, 10, false, null);
            run.addBreak();
            texto(paragrafo.createRun(), rotulos.get(i), 10, false, null);
        }
    }

    private static void texto(XWPFRun run, String conteudo, int tamanho, boolean negrito, String cor) {
        run.setText(conteudo);
        run.setFontFamily(FONTE);
        run.setFontSize(tamanho);
        run.setBold(negrito);
        if (cor != null) {
            run.setColor(cor);
        }
    }
}
