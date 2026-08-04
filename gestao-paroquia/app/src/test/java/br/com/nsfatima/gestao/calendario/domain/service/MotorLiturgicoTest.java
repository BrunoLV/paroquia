package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.model.CorLiturgica;
import br.com.nsfatima.gestao.calendario.domain.model.SaidaLiturgica;
import br.com.nsfatima.gestao.calendario.domain.model.MarcoLiturgico;
import br.com.nsfatima.gestao.calendario.domain.model.TipoSolenidade;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.SolenidadeLiturgicaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
class LiturgicalEngineTest {

    private final MotorLiturgico engine = new MotorLiturgico();

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
    @DisplayName("Deve retornar SaidaLiturgica completo com limites, ciclo e todos os marcos móveis e fixos do banco")
    void deveCalcularAnoLiturgicoApartirDeEntidadesDoBanco() {
        SolenidadeLiturgicaEntity sFixa = new SolenidadeLiturgicaEntity();
        sFixa.setId(UUID.randomUUID());
        sFixa.setChave("SAO_PADRE_PIO");
        sFixa.setNome("São Padre Pio");
        sFixa.setTipoSolenidade(TipoSolenidade.FIXA);
        sFixa.setMesFixo(9);
        sFixa.setDiaFixo(23);
        sFixa.setCorLiturgica("BRANCO");
        sFixa.setFatorDemanda(2.5);
        sFixa.setAtivo(true);

        SolenidadeLiturgicaEntity sMovel = new SolenidadeLiturgicaEntity();
        sMovel.setId(UUID.randomUUID());
        sMovel.setChave("PASCOA");
        sMovel.setNome("Domingo da Ressurreição (Páscoa)");
        sMovel.setTipoSolenidade(TipoSolenidade.MOVEL);
        sMovel.setOffsetDiasPascoa(0);
        sMovel.setCorLiturgica("BRANCO");
        sMovel.setFatorDemanda(3.0);
        sMovel.setAtivo(true);

        SaidaLiturgica output = engine.calculateLiturgicalYear(2026, List.of(sFixa, sMovel));

        assertThat(output).isNotNull();
        assertThat(output.marcosCalculados()).hasSize(2);
        assertThat(output.marcosCalculados().get("SAO_PADRE_PIO").data()).isEqualTo(LocalDate.of(2026, 9, 23));
        assertThat(output.marcosCalculados().get("PASCOA").data()).isEqualTo(LocalDate.of(2026, 4, 5));
    }

    @Test
    @DisplayName("Deve retornar SaidaLiturgica completo com limites, ciclo e todos os marcos móveis e fixos como fallback")
    void deveRetornarLiturgicalOutputCompleto() {
        SaidaLiturgica output = engine.calculateLiturgicalYear(2026);

        assertThat(output).isNotNull();
        assertThat(output.anoNominal()).isEqualTo(2026);
        assertThat(output.cicloLetra()).isEqualTo("A");
        assertThat(output.dataInicio()).isEqualTo(LocalDate.of(2025, 11, 30));
        assertThat(output.dataFim()).isEqualTo(LocalDate.of(2026, 11, 28));

        Map<String, MarcoLiturgico> marcos = output.marcosCalculados();
        assertThat(marcos).isNotNull();

        assertThat(marcos.get("PASCOA").data()).isEqualTo(LocalDate.of(2026, 4, 5));
        assertThat(marcos.get("QUARTA_CINZAS").data()).isEqualTo(LocalDate.of(2026, 2, 18));
        assertThat(marcos.get("QUARTA_CINZAS").corLiturgica()).isEqualTo(CorLiturgica.ROXO);
    }
}
