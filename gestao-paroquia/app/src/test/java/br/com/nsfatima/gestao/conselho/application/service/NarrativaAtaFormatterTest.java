package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.NumeradorAta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static br.com.nsfatima.gestao.conselho.application.service.ImpressaoFixtures.paraImpressao;
import static br.com.nsfatima.gestao.conselho.application.service.ImpressaoFixtures.rascunhoCompleto;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.AGORA;
import static org.assertj.core.api.Assertions.assertThat;

class NarrativaAtaFormatterTest {

    private static final NumeradorAta PRIMEIRA = (organizacao, ano) -> 1;

    @Test
    @DisplayName("abertura traz data por extenso, local, caráter, modalidade, tipo e órgão")
    void abertura() {
        List<String> paragrafos = NarrativaAtaFormatter.paragrafos(paraImpressao(rascunhoCompleto()));

        assertThat(paragrafos.get(0)).isEqualTo("No 1º dia do mês de junho de 2026, no local Salão paroquial, "
                + "realizou-se, em caráter ordinário e de forma presencial, a reunião registrada como “Reunião do "
                + "Conselho Pastoral Paroquial (CPP)” de Conselho Pastoral Paroquial.");
    }

    @Test
    @DisplayName("a presença agrupa presentes e ausentes com nomes e funções")
    void presenca() {
        List<String> paragrafos = NarrativaAtaFormatter.paragrafos(paraImpressao(rascunhoCompleto()));

        assertThat(paragrafos.get(1)).isEqualTo("Estiveram presentes: Maria da Silva e João Pereira (Coordenador(a)). "
                + "Justificaram a ausência: Ana Souza.");
    }

    @Test
    @DisplayName("deliberações trazem resultado, votos e conflito de interesses")
    void deliberacoes() {
        String texto = String.join("\n", NarrativaAtaFormatter.paragrafos(paraImpressao(rascunhoCompleto())));

        assertThat(texto)
                .contains("No item 1 da pauta (Reforma do telhado): Discutiu-se o orçamento.")
                .contains("Deliberação 1.1: Aprovar o orçamento da reforma do telhado. Resultado: parecer favorável "
                        + "(votos: 1 favoráveis, 0 contrários, 0 abstenções).")
                .contains("João Pereira declarou conflito de interesses: Irmão do fornecedor.")
                .contains("Deliberação 2.1: Adiar a compra do som. Resultado: adiada.");
    }

    @Test
    @DisplayName("o encerramento de minuta avisa que não tem efeito até a homologação")
    void encerramentoDeMinuta() {
        List<String> paragrafos = NarrativaAtaFormatter.paragrafos(paraImpressao(rascunhoCompleto()));

        assertThat(paragrafos.get(paragrafos.size() - 1)).contains("Minuta sem efeito até a homologação do Pároco.");
    }

    @Test
    @DisplayName("o encerramento de ata homologada registra a homologação e o quórum é apurado")
    void ataHomologada() {
        AtaReuniao ata = rascunhoCompleto();
        ata.submeter(5, PRIMEIRA, AGORA);
        ata.homologar("paroco", PRIMEIRA, AGORA);

        List<String> paragrafos = NarrativaAtaFormatter.paragrafos(paraImpressao(ata));

        assertThat(String.join("\n", paragrafos))
                .contains("Apurou-se a presença de 2 dos 5 membros convocados; o quórum, de 3 membros, "
                        + "não foi atingido.")
                .contains("e foi homologada pelo Pároco em 01/06/2026.");
    }
}
