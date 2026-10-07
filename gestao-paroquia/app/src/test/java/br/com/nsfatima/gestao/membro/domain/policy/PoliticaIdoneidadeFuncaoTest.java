package br.com.nsfatima.gestao.membro.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.nsfatima.gestao.membro.domain.model.IdoneidadeAgente;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PoliticaIdoneidadeFuncaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 6, 1);
    private static final LocalDate ANTES = LocalDate.of(2025, 1, 1);

    private static IdoneidadeAgente completa() {
        return new IdoneidadeAgente(ANTES, ANTES, ANTES, LocalDate.of(2027, 1, 1), ANTES, "paroco");
    }

    @Test
    void coordenadorSemNenhumRequisitoTemTodasAsPendencias() {
        assertThat(PoliticaIdoneidadeFuncao.pendencias(true, SetorPastoral.SOCIAL_CARIDADE,
                IdoneidadeAgente.vazia(), HOJE)).hasSize(4);
    }

    @Test
    void coordenadorCompletoEApto() {
        assertThat(PoliticaIdoneidadeFuncao.pendencias(true, SetorPastoral.FORMACAO_ENSINAR, completa(), HOJE))
                .isEmpty();
    }

    @Test
    void termoVencidoGeraPendencia() {
        IdoneidadeAgente vencido = new IdoneidadeAgente(ANTES, ANTES, ANTES, LocalDate.of(2026, 1, 1), ANTES,
                "paroco");
        assertThat(PoliticaIdoneidadeFuncao.pendencias(true, SetorPastoral.SOCIAL_CARIDADE, vencido, HOJE))
                .singleElement().asString().contains("Termo de proteção de menores");
    }

    @Test
    void membroDeCatequeseExigeSacramentosETermoMasNaoFormacaoNemEnvio() {
        assertThat(PoliticaIdoneidadeFuncao.pendencias(false, SetorPastoral.FORMACAO_ENSINAR,
                IdoneidadeAgente.vazia(), HOJE)).hasSize(2);
    }

    @Test
    void membroDeLiturgiaExigeApenasVivenciaSacramental() {
        assertThat(PoliticaIdoneidadeFuncao.pendencias(false, SetorPastoral.SANTIFICACAO_LITURGIA,
                IdoneidadeAgente.vazia(), HOJE)).singleElement().asString().contains("sacramental");
    }

    @Test
    void membroDeCaridadeNaoTemExigencia() {
        assertThat(PoliticaIdoneidadeFuncao.pendencias(false, SetorPastoral.SOCIAL_CARIDADE,
                IdoneidadeAgente.vazia(), HOJE)).isEmpty();
    }
}
