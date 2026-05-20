package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.domain.policy.CalendarIntegrityPolicy;
import br.com.nsfatima.gestao.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AddedExtraValidationIntegrationTest {

    private final EventoDomainService service = new EventoDomainService(new CalendarIntegrityPolicy());

    @Test
    void shouldRejectAddedExtraWithoutJustification() {
        assertThrows(IllegalArgumentException.class, () -> service.validateEvento(
                Instant.parse("2026-03-16T10:00:00Z"),
                Instant.parse("2026-03-16T11:00:00Z"),
                EventoStatusInput.ADICIONADO_EXTRA,
                ""));
    }

    @Test
    void shouldAcceptAddedExtraWithJustification() {
        assertDoesNotThrow(() -> service.validateEvento(
                Instant.parse("2026-03-16T10:00:00Z"),
                Instant.parse("2026-03-16T11:00:00Z"),
                EventoStatusInput.ADICIONADO_EXTRA,
                "Ajuste extraordinario aprovado"));
    }
}
