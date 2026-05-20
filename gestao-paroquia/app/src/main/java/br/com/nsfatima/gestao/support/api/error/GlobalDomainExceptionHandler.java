package br.com.nsfatima.gestao.support.api.error;

import br.com.nsfatima.gestao.support.api.v1.error.BaseExceptionHandler;
import br.com.nsfatima.gestao.support.api.v1.error.ErrorCodes;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorItem;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorResponse;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.local.domain.exception.LocalBusinessException;
import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizationBusinessException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Order(2)
public class GlobalDomainExceptionHandler extends BaseExceptionHandler {

    public GlobalDomainExceptionHandler(AuditLogPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    @ExceptionHandler(LocalBusinessException.class)
    public ResponseEntity<ValidationErrorResponse> handleLocalBusiness(LocalBusinessException ex) {
        HttpStatus status = ex.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        return buildValidation(
                status,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(),
                        "local",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(OrganizationBusinessException.class)
    public ResponseEntity<ValidationErrorResponse> handleOrganizationBusiness(OrganizationBusinessException ex) {
        HttpStatus status = ex.getMessage().contains("not found") ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        return buildValidation(
                status,
                ErrorCodes.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.DOMAIN_RULE_VIOLATION.name(),
                        "organization",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handleUsuarioNotFound(UsuarioNotFoundException ex) {
        return buildValidation(
                HttpStatus.NOT_FOUND,
                ErrorCodes.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.RESOURCE_NOT_FOUND.name(),
                        "usuario",
                        ex.getMessage(),
                        null)));
    }
}
