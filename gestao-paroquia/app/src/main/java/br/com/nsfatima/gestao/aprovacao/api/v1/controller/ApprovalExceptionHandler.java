package br.com.nsfatima.gestao.aprovacao.api.v1.controller;

import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalAlreadyDecidedException;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalExecutionFailedException;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalNotFoundException;
import br.com.nsfatima.gestao.calendario.domain.exception.ApprovalRequiredException;
import br.com.nsfatima.gestao.support.api.v1.error.BaseExceptionHandler;
import br.com.nsfatima.gestao.support.api.v1.error.ErrorCodes;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorResponse;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorItem;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;

/**
 * Handles exceptions related to the event approval workflow.
 */
@RestControllerAdvice
@Order(2)
public class ApprovalExceptionHandler extends BaseExceptionHandler {

    public ApprovalExceptionHandler(AuditLogPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    @ExceptionHandler(ApprovalRequiredException.class)
    public ResponseEntity<ValidationErrorResponse> handleApprovalRequired(ApprovalRequiredException ex) {
        return buildValidation(
                HttpStatus.FORBIDDEN,
                ErrorCodes.APPROVAL_REQUIRED,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.APPROVAL_REQUIRED.name(),
                        "aprovacaoId",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ApprovalNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handleApprovalNotFound(ApprovalNotFoundException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.APPROVAL_NOT_FOUND,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.APPROVAL_NOT_FOUND.name(),
                        "aprovacaoId",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ApprovalAlreadyDecidedException.class)
    public ResponseEntity<ValidationErrorResponse> handleApprovalAlreadyDecided(ApprovalAlreadyDecidedException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.APPROVAL_ALREADY_DECIDED,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.APPROVAL_ALREADY_DECIDED.name(),
                        "aprovacaoId",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(ApprovalExecutionFailedException.class)
    public ResponseEntity<ValidationErrorResponse> handleApprovalExecutionFailed(ApprovalExecutionFailedException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.APPROVAL_EXECUTION_FAILED,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.APPROVAL_EXECUTION_FAILED.name(),
                        "approvalExecution",
                        ex.getMessage(),
                        null)));
    }
}
