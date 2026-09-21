package br.com.nsfatima.gestao.calendario.api.error;

import br.com.nsfatima.gestao.calendario.domain.exception.ConflitoAgendaEclesialException;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
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
 * Handler de última instância: {@code ConflitoAgendaEclesialException} (conflito de agenda entre
 * eventos eclesiais) vira 400, e qualquer outra exceção não tratada por um handler mais específico
 * (ver {@link DomainExceptionHandler} e outros {@code @RestControllerAdvice} com {@link Order}
 * numérico menor) vira 500 genérico — este é o único advice sem {@code Order} explícito, portanto
 * o Spring o executa por último entre os advices desta aplicação, atuando como rede de segurança
 * para não vazar stack traces/detalhes internos ao cliente.
 */
@RestControllerAdvice
@Order()
public class GlobalExceptionHandler extends BaseExceptionHandler {

    public GlobalExceptionHandler(AuditoriaPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    @ExceptionHandler(ConflitoAgendaEclesialException.class)
    public ResponseEntity<ValidationErrorResponse> handleConflitoAgendaEclesial(
            ConflitoAgendaEclesialException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Conflito de Agenda Eclesial";
        return buildValidation(
                HttpStatus.CONFLICT,
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
