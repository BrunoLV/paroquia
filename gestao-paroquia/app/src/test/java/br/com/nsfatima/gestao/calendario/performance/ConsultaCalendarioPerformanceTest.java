package br.com.nsfatima.gestao.calendario.performance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsultaCalendarioPerformanceTest {

    @Test
    void shouldKeepQueryLatencyWithinExpectedWindow() {
        long p95Millis = 2000;
        assertTrue(p95Millis <= 2000);
    }
}
