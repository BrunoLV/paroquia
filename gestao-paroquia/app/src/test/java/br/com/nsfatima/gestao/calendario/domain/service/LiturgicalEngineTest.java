package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.model.CorLiturgica;
import br.com.nsfatima.gestao.calendario.domain.model.LiturgicalOutput;
import br.com.nsfatima.gestao.calendario.domain.model.MarcoLiturgico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;

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
    @DisplayName("Deve resolver a letra do ciclo dominical conforme a fórmula oficial (1->A, 2->B, 0->C)")
    void deveResolverCicloLetraConformeFormulaOficial() {
        assertThat(engine.resolveCicloLetra(2024)).isEqualTo("B");
        assertThat(engine.resolveCicloLetra(2025)).isEqualTo("C");
        assertThat(engine.resolveCicloLetra(2026)).isEqualTo("A");
        assertThat(engine.resolveCicloLetra(2027)).isEqualTo("B");
        assertThat(engine.resolveCicloLetra(2028)).isEqualTo("C");
        assertThat(engine.resolveCicloLetra(2030)).isEqualTo("B");
    }

    @Test
    @DisplayName("Deve retornar LiturgicalOutput completo com limites, ciclo e todos os marcos móveis e fixos")
    void deveRetornarLiturgicalOutputCompleto() {
        LiturgicalOutput output = engine.calculateLiturgicalYear(2026);

        assertThat(output).isNotNull();
        assertThat(output.anoNominal()).isEqualTo(2026);
        assertThat(output.cicloLetra()).isEqualTo("A");
        assertThat(output.dataInicio()).isEqualTo(LocalDate.of(2025, 11, 30));
        assertThat(output.dataFim()).isEqualTo(LocalDate.of(2026, 11, 28));

        Map<String, MarcoLiturgico> marcos = output.marcosCalculados();
        assertThat(marcos).isNotNull();

        // Marcos móveis em 2026 (Páscoa em 05/04/2026)
        assertThat(marcos.get("PASCOA").data()).isEqualTo(LocalDate.of(2026, 4, 5));
        assertThat(marcos.get("QUARTA_CINZAS").data()).isEqualTo(LocalDate.of(2026, 2, 18));
        assertThat(marcos.get("QUARTA_CINZAS").corLiturgica()).isEqualTo(CorLiturgica.ROXO);

        assertThat(marcos.get("QUINTA_SANTA").data()).isEqualTo(LocalDate.of(2026, 4, 2));
        assertThat(marcos.get("SABADO_SANTO").data()).isEqualTo(LocalDate.of(2026, 4, 4));
        assertThat(marcos.get("ASCENSAO").data()).isEqualTo(LocalDate.of(2026, 5, 17));
        assertThat(marcos.get("PENTECOSTES").data()).isEqualTo(LocalDate.of(2026, 5, 24));
        assertThat(marcos.get("PENTECOSTES").corLiturgica()).isEqualTo(CorLiturgica.VERMELHO);

        assertThat(marcos.get("SANTISSIMA_TRINDADE").data()).isEqualTo(LocalDate.of(2026, 5, 31));
        assertThat(marcos.get("CORPUS_CHRISTI").data()).isEqualTo(LocalDate.of(2026, 6, 4));
        assertThat(marcos.get("SAGRADO_CORACAO").data()).isEqualTo(LocalDate.of(2026, 6, 12));

        // Solenidades fixas (Santoral)
        assertThat(marcos.get("MAE_DE_DEUS").data()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(marcos.get("SAO_JOSE").data()).isEqualTo(LocalDate.of(2026, 3, 19));
        assertThat(marcos.get("SAO_PEDRO_SAO_PAULO").data()).isEqualTo(LocalDate.of(2026, 6, 29));
        assertThat(marcos.get("SAO_PEDRO_SAO_PAULO").corLiturgica()).isEqualTo(CorLiturgica.VERMELHO);
        assertThat(marcos.get("NATAL_SENHOR").data()).isEqualTo(LocalDate.of(2026, 12, 25));
        assertThat(marcos.get("NATAL_SENHOR").fatorDemanda()).isEqualTo(3.00);
    }
}
