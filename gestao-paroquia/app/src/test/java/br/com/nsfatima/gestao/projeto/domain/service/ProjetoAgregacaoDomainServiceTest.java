package br.com.nsfatima.gestao.projeto.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjetoAgregacaoDomainServiceTest {

    private final ProjetoAgregacaoDomainService service = new ProjetoAgregacaoDomainService();

    @Test
    @DisplayName("Deve calcular percentual de tempo decorrido corretamente")
    void deveCalcularPercentualTempoDecorrido() {
        Instant inicio = Instant.now().minus(8, ChronoUnit.DAYS);
        Instant fim = Instant.now().plus(2, ChronoUnit.DAYS);
        // Total duration: 10 days. 8 days passed -> 80%

        double percentual = service.calcularPercentualTempoDecorrido(inicio, fim);

        assertEquals(80.0, percentual, 0.001);
    }

    @Test
    @DisplayName("Deve indicar em risco quando percentual > 80% e há eventos pendentes")
    void deveIndicarEmRisco() {
        Instant inicio = Instant.now().minus(81, ChronoUnit.DAYS);
        Instant fim = Instant.now().plus(19, ChronoUnit.DAYS);
        // Total duration: 100 days. 81 passed -> 81%
        int eventosPendentes = 1;

        boolean emRisco = service.verificarSeEstaEmRisco(inicio, fim, eventosPendentes);

        assertTrue(emRisco);
    }

    @Test
    @DisplayName("Não deve indicar em risco quando percentual <= 80%")
    void naoDeveIndicarEmRiscoSeTempoAindaSuficiente() {
        Instant inicio = Instant.now().minus(80, ChronoUnit.DAYS);
        Instant fim = Instant.now().plus(20, ChronoUnit.DAYS);
        // Total duration: 100 days. 80 passed -> 80%
        int eventosPendentes = 1;

        boolean emRisco = service.verificarSeEstaEmRisco(inicio, fim, eventosPendentes);

        assertFalse(emRisco);
    }

    @Test
    @DisplayName("Não deve indicar em risco quando não há eventos pendentes")
    void naoDeveIndicarEmRiscoSeSemPendencias() {
        Instant inicio = Instant.now().minus(90, ChronoUnit.DAYS);
        Instant fim = Instant.now().plus(10, ChronoUnit.DAYS);
        // Total duration: 100 days. 90 passed -> 90%
        int eventosPendentes = 0;

        boolean emRisco = service.verificarSeEstaEmRisco(inicio, fim, eventosPendentes);

        assertFalse(emRisco);
    }
}
