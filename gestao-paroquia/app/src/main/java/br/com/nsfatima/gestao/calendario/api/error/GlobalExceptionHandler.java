package br.com.nsfatima.gestao.calendario.api.error;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.support.api.v1.error.BaseExceptionHandler;
import br.com.nsfatima.gestao.support.api.v1.error.ErrorCodes;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorItem;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Fallback exception handler for unexpected system errors.
 */
@RestControllerAdvice
@Order()
public class GlobalExceptionHandler extends BaseExceptionHandler {

    public GlobalExceptionHandler(AuditLogPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    @ExceptionHandler(br.com.nsfatima.gestao.calendario.domain.exception.ConflitoAgendaEclesialException.class)
    public ResponseEntity<ValidationErrorResponse> handleConflitoAgendaEclesial(
            br.com.nsfatima.gestao.calendario.domain.exception.ConflitoAgendaEclesialException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Conflito de Agenda Eclesial";
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.CONFLITO_AGENDA_ECLESIAL,
                msg,
                List.of(new ValidationErrorItem(
                        ErrorCodes.CONFLITO_AGENDA_ECLESIAL.name(),
                        "agenda",
                        msg,
                        null)));
    }

    /**
     * Handles any unexpected system exceptions that were not caught by specific handlers.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationErrorResponse> handleUnexpected(Exception ex) {
        return buildValidation(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                "An unexpected error occurred",
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(),
                        "system",
                        ex.getMessage() != null ? ex.getMessage() : "Unexpected error",
                        null)));
    }
}
