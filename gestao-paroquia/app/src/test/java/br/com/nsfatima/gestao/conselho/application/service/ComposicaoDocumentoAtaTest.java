package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Assinaturas;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Destaque;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Paragrafo;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.QuebraDePagina;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Tabela;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Titulo;
import br.com.nsfatima.gestao.conselho.application.dto.ConteudoExportacao;
import br.com.nsfatima.gestao.conselho.application.dto.DocumentoReuniao;
import br.com.nsfatima.gestao.conselho.application.dto.LayoutExportacao;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.NumeradorAta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static br.com.nsfatima.gestao.conselho.application.service.ImpressaoFixtures.CABECALHO;
import static br.com.nsfatima.gestao.conselho.application.service.ImpressaoFixtures.paraImpressao;
import static br.com.nsfatima.gestao.conselho.application.service.ImpressaoFixtures.rascunhoCompleto;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.AGORA;
import static org.assertj.core.api.Assertions.assertThat;

class ComposicaoDocumentoAtaTest {

    private static final NumeradorAta PRIMEIRA = (organizacao, ano) -> 1;
    private static final ZonedDateTime EMISSAO = ZonedDateTime.of(2026, 6, 2, 14, 5, 0, 0,
            ZoneId.of("America/Sao_Paulo"));

    private static AtaReuniao homologada() {
        AtaReuniao ata = rascunhoCompleto();
        ata.submeter(5, PRIMEIRA, AGORA);
        ata.homologar("paroco", PRIMEIRA, AGORA);
        return ata;
    }

    private static DocumentoReuniao compor(AtaReuniao ata, LayoutExportacao layout, ConteudoExportacao conteudo) {
        return ComposicaoDocumentoAta.compor(CABECALHO, List.of(paraImpressao(ata)), layout, conteudo, EMISSAO);
    }

    private static String textos(DocumentoReuniao documento) {
        StringBuilder texto = new StringBuilder();
        for (BlocoDocumento bloco : documento.blocos()) {
            switch (bloco) {
                case Titulo t -> texto.append(t.texto());
                case Paragrafo p -> texto.append(p.texto());
                case Destaque d -> texto.append(d.texto());
                case Tabela t -> t.linhas().forEach(linha -> texto.append(String.join(" | ", linha)).append('\n'));
                case Assinaturas a -> texto.append(String.join(",", a.rotulos()));
                case QuebraDePagina _ -> texto.append("<pagina>");
            }
            texto.append('\n');
        }
        return texto.toString();
    }

    @Test
    @DisplayName("rascunho sai como minuta, com advertência e sem carimbo de homologação")
    void minuta() {
        DocumentoReuniao documento = compor(rascunhoCompleto(), LayoutExportacao.ESTRUTURADA,
                ConteudoExportacao.ATA_COMPLETA);

        assertThat(documento.minuta()).isTrue();
        assertThat(documento.emissao()).isEqualTo("Documento emitido em 02/06/2026 às 14:05");
        assertThat(textos(documento)).contains("Ata da Reunião do Conselho Pastoral Paroquial — Minuta")
                .contains("MINUTA — SEM EFEITO ATÉ A HOMOLOGAÇÃO OU O REGISTRO")
                .doesNotContain("Homologada pelo Pároco");
    }

    @Test
    @DisplayName("ata homologada sai oficial, numerada, com carimbo e assinatura do Pároco")
    void oficial() {
        DocumentoReuniao documento = compor(homologada(), LayoutExportacao.ESTRUTURADA,
                ConteudoExportacao.ATA_COMPLETA);

        assertThat(documento.minuta()).isFalse();
        assertThat(textos(documento)).contains("Ata da Reunião do Conselho Pastoral Paroquial — nº 001/2026")
                .contains("Homologada pelo Pároco em 01/06/2026.")
                .contains("Redator,Pároco")
                .doesNotContain("MINUTA");
    }

    @Test
    @DisplayName("layout estruturado traz dados, presença em tabela, quórum, itens e deliberações")
    void estruturada() {
        String texto = textos(compor(homologada(), LayoutExportacao.ESTRUTURADA, ConteudoExportacao.ATA_COMPLETA));

        assertThat(texto).contains("Salão paroquial").contains("Maria da Silva | — | Presente")
                .contains("Ana Souza | — | Ausência justificada")
                .contains("Quórum: 2 de 6 membros convocados presentes (mínimo de 4): quórum não atingido.")
                .contains("1. Reforma do telhado")
                .contains("Deliberação 1.1: Aprovar o orçamento da reforma do telhado — Parecer favorável")
                .contains("Conflito de interesses declarado por João Pereira: Irmão do fornecedor");
    }

    @Test
    @DisplayName("layout narrativo traz o texto corrido e não as tabelas")
    void narrativa() {
        String texto = textos(compor(homologada(), LayoutExportacao.NARRATIVA, ConteudoExportacao.ATA_COMPLETA));

        assertThat(texto).contains("No 1º dia do mês de junho de 2026").doesNotContain("Maria da Silva | —");
    }

    @Test
    @DisplayName("relatório de decisões traz só o quadro de decisões, sem assinaturas")
    void decisoes() {
        DocumentoReuniao documento = compor(homologada(), LayoutExportacao.ESTRUTURADA,
                ConteudoExportacao.SOMENTE_DECISOES);
        String texto = textos(documento);

        assertThat(documento.titulo()).isEqualTo("Decisões — Ata da Reunião do Conselho Pastoral Paroquial");
        assertThat(texto).contains("1.1 Reforma do telhado | Aprovar o orçamento da reforma do telhado | "
                        + "Parecer favorável | (favoráveis: 1; contrários: 0; abstenções: 0)")
                .doesNotContain("Redator");
    }

    @Test
    @DisplayName("compilação separa as atas por quebra de página e leva título próprio")
    void compilacao() {
        AtaReuniao a = homologada();
        AtaReuniao b = homologada();

        DocumentoReuniao documento = ComposicaoDocumentoAta.compor(CABECALHO,
                List.of(paraImpressao(a), paraImpressao(b)), LayoutExportacao.ESTRUTURADA,
                ConteudoExportacao.ATA_COMPLETA, EMISSAO);

        assertThat(documento.titulo()).isEqualTo("Livro de atas e decisões");
        assertThat(documento.blocos()).filteredOn(QuebraDePagina.class::isInstance).hasSize(1);
        assertThat(documento.minuta()).isFalse();
    }
}
