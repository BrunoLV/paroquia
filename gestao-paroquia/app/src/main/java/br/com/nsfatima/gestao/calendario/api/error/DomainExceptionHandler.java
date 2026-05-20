package br.com.nsfatima.gestao.support.api.v1.error;

import br.com.nsfatima.gestao.calendario.application.usecase.metrics.CalendarLockedException;
import java.util.List;
import br.com.nsfatima.gestao.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.gestao.calendario.domain.exception.InvalidStatusTransitionException;
import br.com.nsfatima.gestao.projeto.domain.exception.ProjetoNotFoundException;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.IdempotencyConflictException;
import br.com.nsfatima.gestao.calendario.application.usecase.observacao.ObservacaoAutorInvalidoException;
import br.com.nsfatima.gestao.calendario.application.usecase.observacao.ObservacaoNaoEncontradaException;
import br.com.nsfatima.gestao.calendario.application.usecase.observacao.ObservacaoTipoImutavelException;
import br.com.nsfatima.gestao.calendario.application.usecase.observacao.ObservacaoTipoManualInvalidoException;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Handles domain-specific business exceptions.
 */
@RestControllerAdvice
@Order(3)
public class DomainExceptionHandler extends BaseExceptionHandler {

    public DomainExceptionHandler(AuditLogPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    @ExceptionHandler(CalendarLockedException.class)
    public ResponseEntity<ValidationErrorResponse> handleCalendarLocked(CalendarLockedException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.CALENDAR_LOCKED_FOR_YEAR,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.CALENDAR_LOCKED_FOR_YEAR.name(),
                        "status",
                        ex.getMessage(),
                        String.valueOf(ex.getAno()))));
    }

    @ExceptionHandler(EventoNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handleEventoNotFound(EventoNotFoundException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.EVENT_NOT_FOUND,
                "Evento not found",
                List.of(new ValidationErrorItem(
                        ErrorCodes.EVENT_NOT_FOUND.name(),
                        "eventoId",
                        "Event does not exist: " + ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ProjetoNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handleProjetoNotFound(ProjetoNotFoundException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.RESOURCE_NOT_FOUND,
                "Projeto not found",
                List.of(new ValidationErrorItem(
                        ErrorCodes.RESOURCE_NOT_FOUND.name(),
                        "projetoId",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ValidationErrorResponse> handleIdempotencyConflict(IdempotencyConflictException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.IDEMPOTENCY_KEY_CONFLICT,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.IDEMPOTENCY_KEY_CONFLICT.name(),
                        "Idempotency-Key",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ValidationErrorResponse> handleInvalidStatusTransition(InvalidStatusTransitionException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(),
                        "status",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ObservacaoTipoManualInvalidoException.class)
    public ResponseEntity<ValidationErrorResponse> handleObservacaoTipoManualInvalido(
            ObservacaoTipoManualInvalidoException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.OBSERVACAO_TIPO_MANUAL_INVALIDO,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.OBSERVACAO_TIPO_MANUAL_INVALIDO.name(),
                        "tipo",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ObservacaoNaoEncontradaException.class)
    public ResponseEntity<ValidationErrorResponse> handleObservacaoNaoEncontrada(ObservacaoNaoEncontradaException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.OBSERVACAO_NAO_ENCONTRADA,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.OBSERVACAO_NAO_ENCONTRADA.name(),
                        "observacaoId",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ObservacaoAutorInvalidoException.class)
    public ResponseEntity<ValidationErrorResponse> handleObservacaoAutorInvalido(ObservacaoAutorInvalidoException ex) {
        return buildValidation(
                HttpStatus.FORBIDDEN,
                ErrorCodes.OBSERVACAO_AUTOR_INVALIDO,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.OBSERVACAO_AUTOR_INVALIDO.name(),
                        "usuarioId",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ObservacaoTipoImutavelException.class)
    public ResponseEntity<ValidationErrorResponse> handleObservacaoTipoImutavel(ObservacaoTipoImutavelException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.OBSERVACAO_TIPO_IMUTAVEL,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.OBSERVACAO_TIPO_IMUTAVEL.name(),
                        "tipo",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ValidationErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.CONFLICT,
                "Concurrent update conflict: Object of class [%s] with identifier [%s] was updated by another transaction"
                        .formatted(ex.getPersistentClassName(), ex.getIdentifier()),
                List.of(new ValidationErrorItem(
                        ErrorCodes.CONFLICT.name(),
                        "version",
                        "Optimistic locking failed",
                        ex.getIdentifier() == null ? null : String.valueOf(ex.getIdentifier()))));
    }

    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ValidationErrorResponse> handleDataAccess(org.springframework.dao.DataAccessException ex) {
        return buildValidation(
                HttpStatus.SERVICE_UNAVAILABLE,
                ErrorCodes.AUTHZ_SOURCE_UNAVAILABLE,
                "Data source temporarily unavailable",
                List.of(new ValidationErrorItem(
                        ErrorCodes.AUTHZ_SOURCE_UNAVAILABLE.name(),
                        "persistence",
                        ex.getMessage(),
                        null)));
    }
}
