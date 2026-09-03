package br.com.nsfatima.gestao.dashboard.domain.service;

import br.com.nsfatima.gestao.dashboard.domain.model.JanelaTemporal;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class JanelaTemporalResolverTest {

    private final JanelaTemporalResolver resolver = new JanelaTemporalResolver();
    private final Instant referencia = Instant.parse("2026-09-15T13:40:00Z");

    @Test
    void hoje_deveCobrirOApenasODiaCorrenteEmUtc() {
        JanelaTemporal janela = resolver.resolver(PeriodoDashboard.HOJE, referencia);

        assertThat(janela.inicio()).isEqualTo(Instant.parse("2026-09-15T00:00:00Z"));
        assertThat(janela.fim()).isEqualTo(Instant.parse("2026-09-16T00:00:00Z"));
    }

    @Test
    void semana_deveCobrirOsProximosSeteDiasAPartirDaReferencia() {
        JanelaTemporal janela = resolver.resolver(PeriodoDashboard.SEMANA, referencia);

        assertThat(janela.inicio()).isEqualTo(referencia);
        assertThat(Duration.between(janela.inicio(), janela.fim())).isEqualTo(Duration.ofDays(7));
    }

    @Test
    void mes_deveCobrirDoPrimeiroDiaAoInicioDoMesSeguinte() {
        JanelaTemporal janela = resolver.resolver(PeriodoDashboard.MES, referencia);

        assertThat(janela.inicio()).isEqualTo(Instant.parse("2026-09-01T00:00:00Z"));
        assertThat(janela.fim()).isEqualTo(Instant.parse("2026-10-01T00:00:00Z"));
    }

    @Test
    void periodoNulo_deveSerTratadoComoSemana() {
        JanelaTemporal janela = resolver.resolver(null, referencia);

        assertThat(Duration.between(janela.inicio(), janela.fim())).isEqualTo(Duration.ofDays(7));
    }

    @Test
    void contem_deveRespeitarIntervaloSemiaberto() {
        JanelaTemporal janela = resolver.resolver(PeriodoDashboard.HOJE, referencia);

        assertThat(janela.contem(janela.inicio())).isTrue();
        assertThat(janela.contem(janela.fim())).isFalse();
    }
}
