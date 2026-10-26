package br.com.nsfatima.gestao.dashboard.domain.service;

import br.com.nsfatima.gestao.dashboard.domain.model.JanelaTemporal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraOcupacaoAmbienteTest {

    private final CalculadoraOcupacaoAmbiente calculadora = new CalculadoraOcupacaoAmbiente();
    private final JanelaTemporal dia = new JanelaTemporal(
            Instant.parse("2026-09-15T03:00:00Z"), Instant.parse("2026-09-16T03:00:00Z"));

    private static JanelaTemporal r(String inicio, String fim) {
        return new JanelaTemporal(Instant.parse(inicio), Instant.parse(fim));
    }

    @Test
    @DisplayName("Reservas sobrepostas sao unidas e contadas uma unica vez")
    void unePeriodosSobrepostos() {
        List<JanelaTemporal> reservas = List.of(
                r("2026-09-15T12:00:00Z", "2026-09-15T14:00:00Z"),
                r("2026-09-15T13:00:00Z", "2026-09-15T15:00:00Z"),
                r("2026-09-15T12:30:00Z", "2026-09-15T13:00:00Z"));

        assertThat(calculadora.minutosOcupados(reservas, dia)).isEqualTo(180);
    }

    @Test
    @DisplayName("Reservas disjuntas somam os seus periodos")
    void somaPeriodosDisjuntos() {
        List<JanelaTemporal> reservas = List.of(
                r("2026-09-15T12:00:00Z", "2026-09-15T13:00:00Z"),
                r("2026-09-15T16:00:00Z", "2026-09-15T16:30:00Z"));

        assertThat(calculadora.minutosOcupados(reservas, dia)).isEqualTo(90);
    }

    @Test
    @DisplayName("Reserva que atravessa a janela e recortada nos limites do dia")
    void recortaNaJanela() {
        List<JanelaTemporal> reservas = List.of(r("2026-09-14T23:00:00Z", "2026-09-15T05:00:00Z"));

        assertThat(calculadora.minutosOcupados(reservas, dia)).isEqualTo(120);
    }

    @Test
    @DisplayName("Reserva totalmente fora da janela nao conta; sem reservas a ocupacao e zero")
    void ignoraForaDaJanela() {
        assertThat(calculadora.minutosOcupados(
                List.of(r("2026-09-16T03:00:00Z", "2026-09-16T05:00:00Z")), dia)).isZero();
        assertThat(calculadora.minutosOcupados(List.of(), dia)).isZero();
    }
}
