package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.domain.policy.CalendarIntegrityPolicy;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ConflitoAgendaIntegrationTest {

    private final CalendarIntegrityPolicy policy = new CalendarIntegrityPolicy();

    @Test
    void shouldRejectInvalidInterval() {
        assertThrows(IllegalArgumentException.class, () ->
                policy.validateInterval(Instant.parse("2026-03-15T11:00:00Z"), Instant.parse("2026-03-15T10:00:00Z")));
    }
}
