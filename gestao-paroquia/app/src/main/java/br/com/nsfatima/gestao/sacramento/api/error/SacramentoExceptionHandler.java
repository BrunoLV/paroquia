package br.com.nsfatima.gestao.sacramento.api.error;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.sacramento.domain.exception.AssentoImutavelException;
import br.com.nsfatima.gestao.sacramento.domain.exception.AssentoInvalidoException;
import br.com.nsfatima.gestao.sacramento.domain.exception.AssentoNaoEncontradoException;
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
 * Traduz as exceções do contexto de sacramentos: assento inexistente ou reservado inacessível (404),
 * transição vedada pelo estado do assento (409) e requisitos canônicos não atendidos (422, com todas
 * as pendências).
 */
@RestControllerAdvice
@Order(3)
public class SacramentoExceptionHandler extends BaseExceptionHandler {

    public SacramentoExceptionHandler(AuditoriaPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    @ExceptionHandler(AssentoNaoEncontradoException.class)
    public ResponseEntity<ValidationErrorResponse> handleNaoEncontrado(AssentoNaoEncontradoException ex) {
        return buildValidation(HttpStatus.NOT_FOUND, ErrorCodes.RESOURCE_NOT_FOUND, ex.getMessage(),
                List.of(new ValidationErrorItem(ErrorCodes.RESOURCE_NOT_FOUND.name(), "assento", ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(AssentoImutavelException.class)
    public ResponseEntity<ValidationErrorResponse> handleImutavel(AssentoImutavelException ex) {
        return buildValidation(HttpStatus.CONFLICT, ErrorCodes.INVALID_STATUS_TRANSITION, ex.getMessage(),
                List.of(new ValidationErrorItem(ErrorCodes.INVALID_STATUS_TRANSITION.name(), "status",
                        ex.getMessage(), null)));
    }

    @ExceptionHandler(AssentoInvalidoException.class)
    public ResponseEntity<ValidationErrorResponse> handleInvalido(AssentoInvalidoException ex) {
        List<ValidationErrorItem> itens = ex.getPendencias().stream()
                .map(p -> new ValidationErrorItem(ErrorCodes.DOMAIN_RULE_VIOLATION.name(), "assento", p, null))
                .toList();
        return buildValidation(HttpStatus.UNPROCESSABLE_CONTENT, ErrorCodes.DOMAIN_RULE_VIOLATION, ex.getMessage(),
                itens);
    }
}
