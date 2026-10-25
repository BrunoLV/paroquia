package br.com.nsfatima.gestao.conselho.infrastructure.export;

import br.com.nsfatima.gestao.conselho.application.dto.ConteudoExportacao;
import br.com.nsfatima.gestao.conselho.application.dto.DocumentoReuniao;
import br.com.nsfatima.gestao.conselho.application.dto.FormatoExportacaoAta;
import br.com.nsfatima.gestao.conselho.application.dto.LayoutExportacao;
import br.com.nsfatima.gestao.conselho.application.service.ComposicaoDocumentoAta;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static br.com.nsfatima.gestao.conselho.application.service.ImpressaoFixtures.CABECALHO;
import static br.com.nsfatima.gestao.conselho.application.service.ImpressaoFixtures.paraImpressao;
import static br.com.nsfatima.gestao.conselho.application.service.ImpressaoFixtures.rascunhoCompleto;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.AGORA;
import static org.assertj.core.api.Assertions.assertThat;

/** Gera PDF e Word de verdade e confere o conteúdo extraído dos arquivos, nos dois layouts. */
class ExportadoresAtaTest {

    private static final ZonedDateTime EMISSAO = ZonedDateTime.of(2026, 6, 2, 14, 5, 0, 0,
            ZoneId.of("America/Sao_Paulo"));

    private final ExportadorAtaPdf pdf = new ExportadorAtaPdf();
    private final ExportadorAtaDocx docx = new ExportadorAtaDocx();

    private static DocumentoReuniao documento(AtaReuniao ata, LayoutExportacao layout) {
        return ComposicaoDocumentoAta.compor(CABECALHO, List.of(paraImpressao(ata)), layout,
                ConteudoExportacao.ATA_COMPLETA, EMISSAO);
    }

    private static AtaReuniao homologada() {
        AtaReuniao ata = rascunhoCompleto();
        ata.submeter(5, (organizacao, ano) -> 1, AGORA);
        ata.homologar("paroco", (organizacao, ano) -> 1, AGORA);
        return ata;
    }

    private static String textoDoPdf(byte[] arquivo) throws IOException {
        PdfReader leitor = new PdfReader(arquivo);
        PdfTextExtractor extrator = new PdfTextExtractor(leitor);
        StringBuilder texto = new StringBuilder();
        for (int pagina = 1; pagina <= leitor.getNumberOfPages(); pagina++) {
            texto.append(extrator.getTextFromPage(pagina)).append('\n');
        }
        leitor.close();
        return texto.toString();
    }

    private static String textoDoDocx(byte[] arquivo) throws IOException {
        try (XWPFDocument documento = new XWPFDocument(new ByteArrayInputStream(arquivo));
             XWPFWordExtractor extrator = new XWPFWordExtractor(documento)) {
            return extrator.getText();
        }
    }

    @Test
    @DisplayName("cada exportador atende apenas o seu formato")
    void suporta() {
        assertThat(pdf.suporta(FormatoExportacaoAta.PDF)).isTrue();
        assertThat(pdf.suporta(FormatoExportacaoAta.DOCX)).isFalse();
        assertThat(docx.suporta(FormatoExportacaoAta.DOCX)).isTrue();
        assertThat(docx.suporta(FormatoExportacaoAta.PDF)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(LayoutExportacao.class)
    @DisplayName("PDF é válido, leva cabeçalho institucional, conteúdo e carimbo de homologação")
    void pdfOficial(LayoutExportacao layout) throws IOException {
        byte[] arquivo = pdf.exportar(documento(homologada(), layout));

        assertThat(new String(arquivo, 0, 5, StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        String texto = textoDoPdf(arquivo);
        assertThat(texto).contains("Paróquia Nossa Senhora de Fátima").contains("Diocese de São Miguel Paulista")
                .contains("Aprovar o orçamento da reforma do telhado").contains("Documento emitido em 02/06/2026")
                .doesNotContain("MINUTA");
    }

    @Test
    @DisplayName("PDF de minuta leva a marca d'água")
    void pdfMinuta() throws IOException {
        String texto = textoDoPdf(pdf.exportar(documento(rascunhoCompleto(), LayoutExportacao.ESTRUTURADA)));

        assertThat(texto).contains("MINUTA").contains("SEM EFEITO");
    }

    @ParameterizedTest
    @EnumSource(LayoutExportacao.class)
    @DisplayName("Word abre, leva cabeçalho institucional, conteúdo e assinaturas")
    void docxOficial(LayoutExportacao layout) throws IOException {
        String texto = textoDoDocx(docx.exportar(documento(homologada(), layout)));

        assertThat(texto).contains("Paróquia Nossa Senhora de Fátima")
                .contains("Aprovar o orçamento da reforma do telhado").contains("Documento emitido em 02/06/2026")
                .contains("Pároco").doesNotContain("MINUTA");
    }

    @Test
    @DisplayName("Word de minuta leva a faixa de minuta no cabeçalho")
    void docxMinuta() throws IOException {
        String texto = textoDoDocx(docx.exportar(documento(rascunhoCompleto(), LayoutExportacao.NARRATIVA)));

        assertThat(texto).contains("MINUTA — SEM EFEITO");
    }

    @Test
    @DisplayName("compilação gera um único arquivo com várias atas")
    void compilacao() throws IOException {
        DocumentoReuniao doc = ComposicaoDocumentoAta.compor(CABECALHO,
                List.of(paraImpressao(homologada()), paraImpressao(homologada())), LayoutExportacao.ESTRUTURADA,
                ConteudoExportacao.ATA_COMPLETA, EMISSAO);

        assertThat(textoDoPdf(pdf.exportar(doc))).contains("Livro de atas e decisões");
        assertThat(textoDoDocx(docx.exportar(doc))).contains("Livro de atas e decisões");
    }
}
