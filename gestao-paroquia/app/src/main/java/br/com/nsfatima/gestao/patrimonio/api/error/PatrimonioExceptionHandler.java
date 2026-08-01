package br.com.nsfatima.gestao.patrimonio.api.error;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoCancelamentoInvalidoException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoDesalocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoOrganizacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPeriodoConflitanteException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoTransferenciaBloqueadaException;
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

@RestControllerAdvice
@Order(3)
public class PatrimonioExceptionHandler extends BaseExceptionHandler {

    public PatrimonioExceptionHandler(AuditLogPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    @ExceptionHandler(AtivoPatrimonialNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handleNotFound(AtivoPatrimonialNotFoundException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                List.of(new ValidationErrorItem(ErrorCodes.RESOURCE_NOT_FOUND.name(), "ativo", ex.getMessage(), null)));
    }

    @ExceptionHandler(AtivoTransferenciaBloqueadaException.class)
    public ResponseEntity<ValidationErrorResponse> handleTransferenciaBloqueada(
            AtivoTransferenciaBloqueadaException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(), "status", ex.getMessage(), null)));
    }

    @ExceptionHandler(AtivoAlocacaoInvalidaException.class)
    public ResponseEntity<ValidationErrorResponse> handleAlocacaoInvalida(AtivoAlocacaoInvalidaException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(), "alocacao", ex.getMessage(), null)));
    }

    @ExceptionHandler(AtivoOrganizacaoInvalidaException.class)
    public ResponseEntity<ValidationErrorResponse> handleOrganizacaoInvalida(AtivoOrganizacaoInvalidaException ex) {
        return buildValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(), "organizacaoId", ex.getMessage(), null)));
    }

    @ExceptionHandler(AtivoPeriodoConflitanteException.class)
    public ResponseEntity<ValidationErrorResponse> handlePeriodoConflitante(AtivoPeriodoConflitanteException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(), "periodo", ex.getMessage(), null)));
    }

    @ExceptionHandler(AtivoDesalocacaoInvalidaException.class)
    public ResponseEntity<ValidationErrorResponse> handleDesalocacaoInvalida(AtivoDesalocacaoInvalidaException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(), "alocacao", ex.getMessage(), null)));
    }

    @ExceptionHandler(AtivoManutencaoInvalidaException.class)
    public ResponseEntity<ValidationErrorResponse> handleManutencaoInvalida(AtivoManutencaoInvalidaException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(), "manutencao", ex.getMessage(), null)));
    }

    @ExceptionHandler(AtivoAlocacaoCancelamentoInvalidoException.class)
    public ResponseEntity<ValidationErrorResponse> handleCancelamentoInvalido(
            AtivoAlocacaoCancelamentoInvalidoException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.INVALID_STATUS_TRANSITION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.INVALID_STATUS_TRANSITION.name(), "alocacao", ex.getMessage(), null)));
    }
}
