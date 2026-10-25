package br.com.nsfatima.gestao.conselho.infrastructure.export;

import br.com.nsfatima.gestao.conselho.application.dto.CabecalhoParoquia;
import br.com.nsfatima.gestao.conselho.application.dto.DocumentoReuniao;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Image;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfGState;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.IOException;

/**
 * Desenha, em cada página do PDF, o cabeçalho institucional (logotipo, paróquia, diocese e endereço), o rodapé
 * (emissão e número da página) e, nas minutas, a marca d'água diagonal "MINUTA". Fica fora do corpo para que o
 * documento possa fluir por várias páginas sem repetir cabeçalhos manualmente.
 */
class PdfCabecalhoRodapeEvento extends PdfPageEventHelper {

    private static final float MARGEM = 56f;
    private static final float ALTURA_LOGO = 42f;
    private static final Color CINZA = new Color(0x60, 0x60, 0x60);
    private static final Font NOME = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font DETALHE = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL, CINZA);

    private final DocumentoReuniao documento;
    private final Image logo;

    PdfCabecalhoRodapeEvento(DocumentoReuniao documento) {
        this.documento = documento;
        this.logo = carregarLogo(documento.cabecalho());
    }

    private static Image carregarLogo(CabecalhoParoquia cabecalho) {
        if (cabecalho.logo() == null || cabecalho.logo().length == 0) {
            return null;
        }
        try {
            Image imagem = Image.getInstance(cabecalho.logo());
            imagem.scaleToFit(ALTURA_LOGO * 2, ALTURA_LOGO);
            return imagem;
        } catch (IOException | RuntimeException logoInvalido) {
            return null;
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document pdf) {
        if (documento.minuta()) {
            marcaDagua(writer, pdf);
        }
        cabecalho(writer, pdf);
        rodape(writer, pdf);
    }

    private void cabecalho(PdfWriter writer, Document pdf) {
        PdfContentByte tela = writer.getDirectContent();
        float topo = pdf.getPageSize().getHeight() - 36f;
        float x = MARGEM;
        if (logo != null) {
            logo.setAbsolutePosition(MARGEM, topo - ALTURA_LOGO);
            adicionar(tela, logo);
            x += logo.getScaledWidth() + 10f;
        }
        CabecalhoParoquia c = documento.cabecalho();
        ColumnText.showTextAligned(tela, Element.ALIGN_LEFT, new Phrase(c.nome(), NOME), x, topo - 14f, 0);
        ColumnText.showTextAligned(tela, Element.ALIGN_LEFT, new Phrase(c.diocese(), DETALHE), x, topo - 27f, 0);
        ColumnText.showTextAligned(tela, Element.ALIGN_LEFT, new Phrase(c.endereco(), DETALHE), x, topo - 38f, 0);
        tela.setColorStroke(CINZA);
        tela.moveTo(MARGEM, topo - ALTURA_LOGO - 8f);
        tela.lineTo(pdf.getPageSize().getWidth() - MARGEM, topo - ALTURA_LOGO - 8f);
        tela.stroke();
    }

    private void rodape(PdfWriter writer, Document pdf) {
        PdfContentByte tela = writer.getDirectContent();
        float largura = pdf.getPageSize().getWidth();
        tela.setColorStroke(CINZA);
        tela.moveTo(MARGEM, 46f);
        tela.lineTo(largura - MARGEM, 46f);
        tela.stroke();
        ColumnText.showTextAligned(tela, Element.ALIGN_LEFT, new Phrase(documento.emissao(), DETALHE), MARGEM, 34f, 0);
        ColumnText.showTextAligned(tela, Element.ALIGN_RIGHT,
                new Phrase("Página " + writer.getPageNumber(), DETALHE), largura - MARGEM, 34f, 0);
    }

    private static void marcaDagua(PdfWriter writer, Document pdf) {
        PdfContentByte fundo = writer.getDirectContentUnder();
        PdfGState translucido = new PdfGState();
        translucido.setFillOpacity(0.12f);
        fundo.saveState();
        fundo.setGState(translucido);
        BaseFont fonte;
        try {
            fonte = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (IOException | DocumentException indisponivel) {
            fundo.restoreState();
            return;
        }
        fundo.beginText();
        fundo.setFontAndSize(fonte, 110f);
        fundo.setColorFill(new Color(0xB0, 0x20, 0x20));
        fundo.showTextAligned(Element.ALIGN_CENTER, "MINUTA", pdf.getPageSize().getWidth() / 2,
                pdf.getPageSize().getHeight() / 2, 45f);
        fundo.endText();
        fundo.restoreState();
    }

    private static void adicionar(PdfContentByte tela, Image imagem) {
        try {
            tela.addImage(imagem);
        } catch (DocumentException semEfeito) {
            return;
        }
    }
}
