package br.com.nsfatima.gestao.calendario.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LiturgicalEngineTest {

    private final LiturgicalEngine engine = new LiturgicalEngine();

    @Test
    @DisplayName("Deve calcular a data da Páscoa corretamente usando algoritmo Meeus/Jones/Butcher")
    void deveCalcularPascoa() {
        assertThat(engine.calculateEaster(2026)).isEqualTo(LocalDate.of(2026, 4, 5));
        assertThat(engine.calculateEaster(2027)).isEqualTo(LocalDate.of(2027, 3, 28));
        assertThat(engine.calculateEaster(2028)).isEqualTo(LocalDate.of(2028, 4, 16));
    }

    @Test
    @DisplayName("Deve calcular o 1º Domingo do Advento para o ano litúrgico")
    void deveCalcularInicioAdvento() {
        ZonedDateTime advento2026 = engine.calculateAdventoInicio(2026);
        assertThat(advento2026.toLocalDate()).isEqualTo(LocalDate.of(2025, 11, 30));

        ZonedDateTime advento2027 = engine.calculateAdventoInicio(2027);
        assertThat(advento2027.toLocalDate()).isEqualTo(LocalDate.of(2026, 11, 29));
    }

    @Test
    @DisplayName("Deve resolver o ano litúrgico nominal para datas que caem após o Advento")
    void deveResolverAnoLiturgicoParaDataNoAdvento() {
        Instant dez2026 = Instant.parse("2026-12-10T10:00:00Z");
        assertThat(engine.resolveAnoLiturgico(dez2026)).isEqualTo(2027);

        Instant jan2026 = Instant.parse("2026-01-15T10:00:00Z");
        assertThat(engine.resolveAnoLiturgico(jan2026)).isEqualTo(2026);
    }

    @Test
    @DisplayName("Deve resolver a letra do ciclo (A, B, C) a partir do ano litúrgico")
    void deveResolverCicloLetra() {
        assertThat(engine.resolveCicloLetra(2026)).isEqualTo("C");
        assertThat(engine.resolveCicloLetra(2027)).isEqualTo("A");
        assertThat(engine.resolveCicloLetra(2028)).isEqualTo("B");
        assertThat(engine.resolveCicloLetra(2029)).isEqualTo("C");
    }
}
