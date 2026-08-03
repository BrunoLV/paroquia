package br.com.nsfatima.gestao.calendario.api.error;

import br.com.nsfatima.gestao.calendario.domain.exception.ConflitoAgendaEclesialException;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.support.api.v1.error.ErrorCodes;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ConflitoAgendaEclesialExceptionTest {

    @Test
    @DisplayName("Deve tratar ConflitoAgendaEclesialException retornando HTTP 400 Bad Request")
    void deveTratarConflitoAgendaEclesialException() {
        AuditoriaPersistenceService auditLog = Mockito.mock(AuditoriaPersistenceService.class);
        GlobalExceptionHandler handler = new GlobalExceptionHandler(auditLog);

        ConflitoAgendaEclesialException exception = new ConflitoAgendaEclesialException("Conflito de Agenda Eclesial");
        ResponseEntity<ValidationErrorResponse> response = handler.handleConflitoAgendaEclesial(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ErrorCodes.CONFLITO_AGENDA_ECLESIAL.name(), response.getBody().errorCode());
        assertEquals("Conflito de Agenda Eclesial", response.getBody().message());
    }
}
