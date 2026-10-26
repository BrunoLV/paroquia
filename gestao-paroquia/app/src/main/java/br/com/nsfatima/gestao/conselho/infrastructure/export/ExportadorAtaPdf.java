package br.com.nsfatima.gestao.conselho.infrastructure.export;

import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Assinaturas;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Destaque;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Paragrafo;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.QuebraDePagina;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Tabela;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Titulo;
import br.com.nsfatima.gestao.conselho.application.dto.DocumentoReuniao;
import br.com.nsfatima.gestao.conselho.application.dto.FormatoExportacaoAta;
import br.com.nsfatima.gestao.conselho.application.port.out.ExportadorAta;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Desenha o documento da ata em PDF (OpenPDF). Recebe blocos neutros já compostos, de modo que o texto é o mesmo
 * do Word; aqui só há tipografia. Cabeçalho, rodapé e marca de minuta ficam em {@link PdfCabecalhoRodapeEvento}.
 */
@Component
public class ExportadorAtaPdf implements ExportadorAta {

    private static final float MARGEM_LATERAL = 56f;
    private static final float MARGEM_SUPERIOR = 118f;
    private static final float MARGEM_INFERIOR = 64f;
    private static final Color FUNDO_CABECALHO = new Color(0xEE, 0xEE, 0xEE);
    private static final Font TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 10.5f);
    private static final Font NEGRITO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10.5f);
    private static final Font FONTE_DESTAQUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11f, Font.NORMAL,
            new Color(0xB0, 0x20, 0x20));

    @Override
    public boolean suporta(FormatoExportacaoAta formato) {
        return formato == FormatoExportacaoAta.PDF;
    }

    @Override
    public byte[] exportar(DocumentoReuniao documento) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        Document pdf = new Document(PageSize.A4, MARGEM_LATERAL, MARGEM_LATERAL, MARGEM_SUPERIOR, MARGEM_INFERIOR);
        try {
            PdfWriter escritor = PdfWriter.getInstance(pdf, saida);
            escritor.setPageEvent(new PdfCabecalhoRodapeEvento(documento));
            pdf.addTitle(documento.titulo());
            pdf.open();
            for (BlocoDocumento bloco : documento.blocos()) {
                pdf.add(elemento(bloco));
            }
            pdf.close();
        } catch (DocumentException falha) {
            throw new IllegalStateException("Falha ao gerar o PDF da ata.", falha);
        }
        return saida.toByteArray();
    }

    private static Element elemento(BlocoDocumento bloco) {
        return switch (bloco) {
            case Titulo t -> titulo(t);
            case Paragrafo p -> paragrafo(p.texto());
            case Destaque d -> destaque(d.texto());
            case Tabela t -> tabela(t);
            case Assinaturas a -> assinaturas(a.rotulos());
            case QuebraDePagina _ -> Chunk.NEXTPAGE;
        };
    }

    private static Paragraph titulo(Titulo t) {
        float tamanho = switch (t.nivel()) {
            case 1 -> 15f;
            case 2 -> 13f;
            default -> 11.5f;
        };
        Paragraph titulo = new Paragraph(t.texto(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, tamanho));
        titulo.setAlignment(t.nivel() == 1 ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
        titulo.setSpacingBefore(t.nivel() == 1 ? 4f : 10f);
        titulo.setSpacingAfter(6f);
        return titulo;
    }

    private static Paragraph paragrafo(String texto) {
        Paragraph paragrafo = new Paragraph(texto, TEXTO);
        paragrafo.setAlignment(Element.ALIGN_JUSTIFIED);
        paragrafo.setSpacingAfter(6f);
        paragrafo.setLeading(15f);
        return paragrafo;
    }

    private static Paragraph destaque(String texto) {
        Paragraph aviso = new Paragraph(texto, FONTE_DESTAQUE);
        aviso.setAlignment(Element.ALIGN_CENTER);
        aviso.setSpacingBefore(4f);
        aviso.setSpacingAfter(8f);
        return aviso;
    }

    private static PdfPTable tabela(Tabela t) {
        int colunas = t.cabecalho().isEmpty() ? t.linhas().get(0).size() : t.cabecalho().size();
        PdfPTable tabela = new PdfPTable(colunas);
        tabela.setWidthPercentage(100f);
        tabela.setSpacingBefore(4f);
        tabela.setSpacingAfter(8f);
        t.cabecalho().forEach(titulo -> tabela.addCell(celula(titulo, NEGRITO, FUNDO_CABECALHO)));
        for (List<String> linha : t.linhas()) {
            for (int i = 0; i < linha.size(); i++) {
                boolean rotulo = t.cabecalho().isEmpty() && i == 0;
                tabela.addCell(celula(linha.get(i), rotulo ? NEGRITO : TEXTO, rotulo ? FUNDO_CABECALHO : null));
            }
        }
        return tabela;
    }

    private static PdfPCell celula(String texto, Font fonte, Color fundo) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setPadding(5f);
        if (fundo != null) {
            celula.setBackgroundColor(fundo);
        }
        return celula;
    }

    private static PdfPTable assinaturas(List<String> rotulos) {
        PdfPTable tabela = new PdfPTable(rotulos.size());
        tabela.setWidthPercentage(100f);
        tabela.setSpacingBefore(46f);
        for (String rotulo : rotulos) {
            PdfPCell celula = new PdfPCell(new Phrase(rotulo, TEXTO));
            celula.setBorder(Rectangle.TOP);
            celula.setHorizontalAlignment(Element.ALIGN_CENTER);
            celula.setPaddingTop(4f);
            tabela.addCell(celula);
        }
        return tabela;
    }
}
