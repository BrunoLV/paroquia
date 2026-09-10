package br.com.nsfatima.gestao.calendario.api.error;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.PapelEscopoInvalidoException;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.support.api.v1.error.BaseExceptionHandler;
import br.com.nsfatima.gestao.support.api.v1.error.ErrorCodes;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorItem;
import br.com.nsfatima.gestao.support.api.v1.error.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Handles security-related exceptions like access denial and authentication failures.
 */
@RestControllerAdvice
@Order(1)
public class SecurityExceptionHandler extends BaseExceptionHandler {

    public SecurityExceptionHandler(AuditoriaPersistenceService auditLogPersistenceService) {
        super(auditLogPersistenceService);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ValidationErrorResponse> handleForbiddenOperation(
            ForbiddenOperationException ex,
            HttpServletRequest request) {
        logSecurityDenial(ErrorCodes.FORBIDDEN, ex.getMessage(), request);
        return buildValidation(
                HttpStatus.FORBIDDEN,
                ErrorCodes.FORBIDDEN,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.FORBIDDEN.name(),
                        "authorization",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(PapelEscopoInvalidoException.class)
    public ResponseEntity<ValidationErrorResponse> handleRoleScopeInvalid(
            PapelEscopoInvalidoException ex,
            HttpServletRequest request) {
        logSecurityDenial(ErrorCodes.ROLE_SCOPE_INVALID, ex.getMessage(), request);
        return buildValidation(
                HttpStatus.FORBIDDEN,
                ErrorCodes.ROLE_SCOPE_INVALID,
                ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.ROLE_SCOPE_INVALID.name(),
                        "authorization",
                        ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ValidationErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        logSecurityDenial(ErrorCodes.ACCESS_DENIED, ex.getMessage(), request);
        return buildValidation(
                HttpStatus.FORBIDDEN,
                ErrorCodes.ACCESS_DENIED,
                ex.getMessage() == null ? "Access denied" : ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.ACCESS_DENIED.name(),
                        "authorization",
                        ex.getMessage() == null ? "Access denied" : ex.getMessage(),
                        null)));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ValidationErrorResponse> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request) {
        logSecurityDenial(ErrorCodes.AUTH_REQUIRED, ex.getMessage(), request);
        return buildValidation(
                HttpStatus.UNAUTHORIZED,
                ErrorCodes.AUTH_REQUIRED,
                ex.getMessage() == null ? "Authentication required" : ex.getMessage(),
                List.of(new ValidationErrorItem(
                        ErrorCodes.AUTH_REQUIRED.name(),
                        "authorization",
                        ex.getMessage() == null ? "Authentication required" : ex.getMessage(),
                        null)));
    }
}
