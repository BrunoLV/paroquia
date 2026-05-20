package br.com.nsfatima.gestao.support.api.v1.error;
import br.com.nsfatima.gestao.iam.infrastructure.security.ExternalMembershipReader;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Base class for exception handlers providing common utilities for response building and logging.
 */
public abstract class BaseExceptionHandler {

    protected final AuditLogPersistenceService auditLogPersistenceService;

    protected BaseExceptionHandler(AuditLogPersistenceService auditLogPersistenceService) {
        this.auditLogPersistenceService = auditLogPersistenceService;
    }

    /**
     * Builds a standardized validation response.
     * 
     * Example: buildValidation(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_ERROR, "Invalid data", items);
     */
    protected ResponseEntity<ValidationErrorResponse> buildValidation(
            HttpStatus status,
            ErrorCodes errorCode,
            String message,
            List<ValidationErrorItem> errors) {
        return ResponseEntity.status(status.value()).body(new ValidationErrorResponse(
                errorCode.name(),
                message,
                resolveCorrelationId(),
                errors));
    }

    protected void logSecurityDenial(ErrorCodes errorCode, String message, HttpServletRequest request) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("endpoint", request == null ? "n/a" : request.getRequestURI());
        metadata.put("result", "DENY");
        metadata.put("errorCode", errorCode.name());
        metadata.put("errorMessage", message);

        String userId = resolveUserId();
        if (userId != null) {
            metadata.put("userId", userId);
        }

        String organizationId = resolveOrganizationId();
        if (organizationId != null) {
            metadata.put("organizationId", organizationId);
        }

        auditLogPersistenceService.log(
                resolveActor(),
                "security-denied",
                request == null ? "n/a" : request.getRequestURI(),
                "DENY",
                metadata);
    }

    protected String resolveActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "anonymous";
        }
        return authentication.getName();
    }

    protected String resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            return null;
        }
        return usuarioDetails.getUsuarioId().toString();
    }

    protected String resolveOrganizationId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioDetails usuarioDetails)) {
            return null;
        }
        return usuarioDetails.primaryMembership()
                .map(membership -> membership.organizationId().toString())
                .orElse(null);
    }

    protected String resolveCorrelationId() {
        String correlationId = MDC.get("correlationId");
        return correlationId == null || correlationId.isBlank() ? "n/a" : correlationId;
    }
}
