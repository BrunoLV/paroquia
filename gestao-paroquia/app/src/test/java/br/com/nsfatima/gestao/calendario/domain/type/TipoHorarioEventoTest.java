package br.com.nsfatima.gestao.calendario.domain.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TipoHorarioEventoTest {

    private final ZoneId zoneSp = ZoneId.of("America/Sao_Paulo");
    private final LocalDate data = LocalDate.of(2026, 10, 15);

    @Test
    @DisplayName("Deve calcular corretamente os limites para Dia Inteiro")
    void deveCalcularLimitesDiaInteiro() {
        TipoHorarioEvento tipo = TipoHorarioEvento.DIA_INTEIRO;
        assertThat(tipo.isPredefinido()).isTrue();
        assertThat(tipo.getBufferMinutos()).isZero();

        var intervalo = tipo.calcularIntervalo(data, zoneSp);
        // Em America/Sao_Paulo (UTC-3):
        // 2026-10-15T00:00:00-03:00 -> 2026-10-15T03:00:00Z
        // 2026-10-15T23:59:59-03:00 -> 2026-10-16T02:59:59Z
        assertThat(intervalo.inicioUtc()).isEqualTo("2026-10-15T03:00:00Z");
        assertThat(intervalo.fimUtc()).isEqualTo("2026-10-16T02:59:59Z");
    }

    @Test
    @DisplayName("Deve calcular corretamente os limites para Madrugada (00:00 às 05:59)")
    void deveCalcularLimitesMadrugada() {
        TipoHorarioEvento tipo = TipoHorarioEvento.MADRUGADA;
        assertThat(tipo.isPredefinido()).isTrue();
        assertThat(tipo.getBufferMinutos()).isZero();

        var intervalo = tipo.calcularIntervalo(data, zoneSp);
        assertThat(intervalo.inicioUtc()).isEqualTo("2026-10-15T03:00:00Z");
        assertThat(intervalo.fimUtc()).isEqualTo("2026-10-15T08:59:59Z");
    }

    @Test
    @DisplayName("Deve calcular corretamente os limites para Manhã (06:00 às 11:59)")
    void deveCalcularLimitesManha() {
        TipoHorarioEvento tipo = TipoHorarioEvento.MANHA;
        assertThat(tipo.isPredefinido()).isTrue();
        assertThat(tipo.getBufferMinutos()).isZero();

        var intervalo = tipo.calcularIntervalo(data, zoneSp);
        assertThat(intervalo.inicioUtc()).isEqualTo("2026-10-15T09:00:00Z");
        assertThat(intervalo.fimUtc()).isEqualTo("2026-10-15T14:59:59Z");
    }

    @Test
    @DisplayName("Deve calcular corretamente os limites para Tarde (12:00 às 17:59)")
    void deveCalcularLimitesTarde() {
        TipoHorarioEvento tipo = TipoHorarioEvento.TARDE;
        assertThat(tipo.isPredefinido()).isTrue();
        assertThat(tipo.getBufferMinutos()).isZero();

        var intervalo = tipo.calcularIntervalo(data, zoneSp);
        assertThat(intervalo.inicioUtc()).isEqualTo("2026-10-15T15:00:00Z");
        assertThat(intervalo.fimUtc()).isEqualTo("2026-10-15T20:59:59Z");
    }

    @Test
    @DisplayName("Deve calcular corretamente os limites para Noite (18:00 às 23:59)")
    void deveCalcularLimitesNoite() {
        TipoHorarioEvento tipo = TipoHorarioEvento.NOITE;
        assertThat(tipo.isPredefinido()).isTrue();
        assertThat(tipo.getBufferMinutos()).isZero();

        var intervalo = tipo.calcularIntervalo(data, zoneSp);
        assertThat(intervalo.inicioUtc()).isEqualTo("2026-10-15T21:00:00Z");
        assertThat(intervalo.fimUtc()).isEqualTo("2026-10-16T02:59:59Z");
    }

    @Test
    @DisplayName("Personalizado deve ter buffer de 30 minutos e lançar exceção ao calcular intervalo automático")
    void personalizadoDeveTerBufferEProibirCalculoAutomatico() {
        TipoHorarioEvento tipo = TipoHorarioEvento.PERSONALIZADO;
        assertThat(tipo.isPredefinido()).isFalse();
        assertThat(tipo.getBufferMinutos()).isEqualTo(30);

        assertThatThrownBy(() -> tipo.calcularIntervalo(data, zoneSp))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cálculo automático de faixa só se aplica a formatos pré-definidos");
    }
}
