package br.com.nsfatima.gestao.aprovacao.api.v1.controller;

import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoJaDecididaException;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoExecucaoFalhouException;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoNaoEncontradaException;
import br.com.nsfatima.gestao.calendario.domain.exception.AprovacaoObrigatoriaException;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaPersistenceService;
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
 * Handles exceptions related to the event aprovacao workflow.
 */
@RestControllerAdvice
@Order(2)
public class AprovacaoExceptionHandler extends BaseExceptionHandler {

    private static final String FIELD_APROVACAO_ID = "aprovacaoId";

    public AprovacaoExceptionHandler(AuditoriaPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    @ExceptionHandler(AprovacaoObrigatoriaException.class)
    public ResponseEntity<ValidationErrorResponse> handleAprovacaoRequired(AprovacaoObrigatoriaException ex) {
        return buildValidation(
                HttpStatus.FORBIDDEN,
                ErrorCodes.APROVACAO_OBRIGATORIA,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.APROVACAO_OBRIGATORIA.name(),
                        FIELD_APROVACAO_ID,
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(AprovacaoNaoEncontradaException.class)
    public ResponseEntity<ValidationErrorResponse> handleAprovacaoNotFound(AprovacaoNaoEncontradaException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.APROVACAO_NAO_ENCONTRADA,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.APROVACAO_NAO_ENCONTRADA.name(),
                        FIELD_APROVACAO_ID,
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(AprovacaoJaDecididaException.class)
    public ResponseEntity<ValidationErrorResponse> handleAprovacaoAlreadyDecided(AprovacaoJaDecididaException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.APROVACAO_JA_DECIDIDA,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.APROVACAO_JA_DECIDIDA.name(),
                        FIELD_APROVACAO_ID,
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(AprovacaoExecucaoFalhouException.class)
    public ResponseEntity<ValidationErrorResponse> handleAprovacaoExecutionFailed(AprovacaoExecucaoFalhouException ex) {
        return buildValidation(
                HttpStatus.CONFLICT,
                ErrorCodes.APROVACAO_EXECUCAO_FALHOU,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.APROVACAO_EXECUCAO_FALHOU.name(),
                        "aprovacaoExecution",
                        ex.getMessage(),
                        null)));
    }
}
