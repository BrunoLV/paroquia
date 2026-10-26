package br.com.nsfatima.gestao.support.domain.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class RelogioNegocioTest {

    private static final ZoneId BRT = ZoneId.of("America/Sao_Paulo");

    private static RelogioNegocio em(String instanteUtc) {
        return new RelogioNegocio(Clock.fixed(Instant.parse(instanteUtc), ZoneId.of("UTC")), BRT);
    }

    @Test
    @DisplayName("31/12 22h BRT ainda e o ano antigo, embora em UTC ja seja 01/01")
    void anoNaViradaEmBrt() {
        RelogioNegocio relogio = em("2027-01-01T01:00:00Z");

        assertThat(relogio.hoje()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(relogio.anoDe(relogio.agora())).isEqualTo(2026);
    }

    @Test
    @DisplayName("21h-24h BRT do ultimo dia do mes ainda pertencem ao mes corrente")
    void ultimoDiaDoMesApos21h() {
        RelogioNegocio relogio = em("2026-04-01T02:30:00Z");

        assertThat(relogio.hoje()).isEqualTo(LocalDate.of(2026, 3, 31));
        assertThat(relogio.inicioDoMes()).isEqualTo(Instant.parse("2026-03-01T03:00:00Z"));
    }

    @Test
    @DisplayName("29/02 de ano bissexto e resolvido no fuso da paroquia")
    void vinteENoveDeFevereiro() {
        RelogioNegocio relogio = em("2028-03-01T01:00:00Z");

        assertThat(relogio.hoje()).isEqualTo(LocalDate.of(2028, 2, 29));
    }

    @Test
    @DisplayName("Instante de inicio do dia e convertido a partir do dia civil de Brasilia")
    void inicioDoDia() {
        RelogioNegocio relogio = em("2026-06-10T12:00:00Z");

        assertThat(relogio.inicioDoDia(LocalDate.of(2026, 6, 10))).isEqualTo(Instant.parse("2026-06-10T03:00:00Z"));
        assertThat(relogio.dataDe(Instant.parse("2026-06-10T02:59:59Z"))).isEqualTo(LocalDate.of(2026, 6, 9));
    }

    @Test
    @DisplayName("Ocorrencia das 22h30 BRT mantem o horario local mesmo quando em UTC cai no dia seguinte")
    void mesmoHorarioNoDiaPreservaHorarioLocal() {
        RelogioNegocio relogio = em("2026-06-10T12:00:00Z");
        Instant sabado2230 = Instant.parse("2026-01-04T01:30:00Z");

        assertThat(relogio.dataDe(sabado2230)).isEqualTo(LocalDate.of(2026, 1, 3));
        assertThat(relogio.mesmoHorarioNoDia(sabado2230, LocalDate.of(2026, 1, 10)))
                .isEqualTo(Instant.parse("2026-01-11T01:30:00Z"));
    }
}
