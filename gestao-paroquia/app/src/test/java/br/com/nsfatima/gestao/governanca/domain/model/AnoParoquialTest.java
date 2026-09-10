package br.com.nsfatima.gestao.governanca.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnoParoquialTest {

    private static final Instant AGORA = Instant.parse("2026-06-01T12:00:00Z");
    private static final int ANO_CORRENTE = 2026;

    @Test
    void abrirPlanejamentoIniciaEmPlanejamento() {
        AnoParoquial ano = AnoParoquial.abrirPlanejamento(2027, AGORA);
        assertThat(ano.getStatus()).isEqualTo(AnoParoquialStatus.EM_PLANEJAMENTO);
        assertThat(ano.getDataFechamento()).isNull();
    }

    @Test
    void fecharAnoCorrenteRegistraDataFechamento() {
        AnoParoquial ano = AnoParoquial.abrirPlanejamento(ANO_CORRENTE, AGORA);
        ano.fechar(ANO_CORRENTE, AGORA);
        assertThat(ano.getStatus()).isEqualTo(AnoParoquialStatus.FECHADO);
        assertThat(ano.getDataFechamento()).isEqualTo(AGORA);
    }

    @Test
    void fecharAnoNaoCorrenteEmPlanejamentoFalha() {
        AnoParoquial ano = AnoParoquial.abrirPlanejamento(2025, AGORA);
        assertThatThrownBy(() -> ano.fechar(ANO_CORRENTE, AGORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ano corrente");
    }

    @Test
    void anoConcluidoEImutavel() {
        AnoParoquial ano = AnoParoquial.abrirPlanejamento(ANO_CORRENTE, AGORA);
        ano.concluir(ANO_CORRENTE, AGORA);
        assertThatThrownBy(() -> ano.fechar(ANO_CORRENTE, AGORA))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ano.reabrirPlanejamento(AGORA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void concluirEIdempotente() {
        AnoParoquial ano = AnoParoquial.abrirPlanejamento(ANO_CORRENTE, AGORA);
        ano.concluir(ANO_CORRENTE, AGORA);
        ano.concluir(ANO_CORRENTE, AGORA);
        assertThat(ano.getStatus()).isEqualTo(AnoParoquialStatus.CONCLUIDO);
    }

    @Test
    void reabrirPlanejamentoLimpaDataFechamento() {
        AnoParoquial ano = new AnoParoquial(ANO_CORRENTE, AnoParoquialStatus.FECHADO, AGORA, AGORA);
        ano.reabrirPlanejamento(AGORA);
        assertThat(ano.getStatus()).isEqualTo(AnoParoquialStatus.EM_PLANEJAMENTO);
        assertThat(ano.getDataFechamento()).isNull();
    }

    @Test
    void concluirAnoJaFechadoNaoExigeAnoCorrente() {
        AnoParoquial ano = new AnoParoquial(2025, AnoParoquialStatus.FECHADO, AGORA, AGORA);
        ano.concluir(ANO_CORRENTE, AGORA);
        assertThat(ano.getStatus()).isEqualTo(AnoParoquialStatus.CONCLUIDO);
    }
}
