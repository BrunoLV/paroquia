package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;

@Component
public class EventoAuditPublisher implements ApprovalAuditPublisher {

    private final AuditLogPersistenceService auditLogPersistenceService;

    public EventoAuditPublisher(AuditLogPersistenceService auditLogPersistenceService) {
        this.auditLogPersistenceService = auditLogPersistenceService;
    }

    /**
     * Publishes a simple audit log entry.
     * 
     * Usage Example:
     * publisher.publish("admin", "read", "evento:123", "success");
     */
    public void publish(String actor, String action, String target, String result) {
        auditLogPersistenceService.log(actor, action, target, result, withDefaults(resolveResourceType(action), target, Map.of()));
    }

    /**
     * Publishes an audit log entry with additional metadata.
     * 
     * Usage Example:
     * publisher.publish("admin", "update", "evento:123", "success", Map.of("fields", List.of("titulo")));
     */
    public void publish(String actor, String action, String target, String result, Map<String, Object> metadata) {
        String resourceType = metadata != null && metadata.get("resourceType") != null
                ? String.valueOf(metadata.get("resourceType"))
                : resolveResourceType(action);
        auditLogPersistenceService.log(actor, action, target, result, withDefaults(resourceType, target, metadata));
    }

    /**
     * Records a successful event creation.
     */
    public void publishCreateSuccess(String actor, String target, boolean replay, String conflictState) {
        auditLogPersistenceService.log(
                actor,
                "create",
                target,
                "success",
                withDefaults("EVENTO", target, Map.of(
                        "replay", replay,
                        "conflictState", conflictState == null ? "NONE" : conflictState)));
    }

    /**
     * Records a failed event creation.
     */
    public void publishCreateFailure(String actor, String errorCategory, String message) {
        auditLogPersistenceService.log(
                actor,
                "create",
                "evento",
                "failure",
                withDefaults("EVENTO", "evento", Map.of(
                        "errorCategory", errorCategory,
                        "message", message)));
    }

    /**
     * Records a successful event listing.
     */
    public void publishListSuccess(String actor, int totalItems) {
        auditLogPersistenceService.log(actor, "list", "eventos", "success", Map.of("totalItems", totalItems));
    }

    /**
     * Records a denied write attempt.
     */
    public void publishDeniedWrite(String actor, String target) {
        auditLogPersistenceService.log(actor, "write-denied", target, "ACCESS_DENIED", Map.of("errorCategory", "AUTHZ"));
    }

    /**
     * Records a pending event cancellation request.
     */
    public void publishCancellationPending(String actor, String eventoId, String solicitacaoAprovacaoId,
            String motivo) {
        auditLogPersistenceService.log(
                actor,
                "cancel",
                eventoId,
                "pending",
                withDefaults("EVENTO", eventoId, Map.of(
                        "solicitacaoAprovacaoId", solicitacaoAprovacaoId,
                        "canceladoMotivo", motivo)));
    }

    /**
     * Records a rejected cancellation request.
     */
    public void publishCancellationRejected(String actor, String aprovacaoId, String eventoId) {
        auditLogPersistenceService.log(
                actor,
                "approval-decision",
                aprovacaoId,
                "rejected",
                withDefaults("APROVACAO", aprovacaoId, Map.of("eventoId", eventoId, "aprovacaoId", aprovacaoId)));
    }

    /**
     * Records a successful automatic execution of a cancellation.
     */
    public void publishCancellationExecuted(String actor, String aprovacaoId, String eventoId) {
        auditLogPersistenceService.log(
                actor,
                "approval-decision",
                aprovacaoId,
                "executed",
                withDefaults("APROVACAO", aprovacaoId, Map.of("eventoId", eventoId, "aprovacaoId", aprovacaoId)));
    }

    /**
     * Records a failure during automatic cancellation execution.
     */
    public void publishCancellationExecutionFailed(String actor, String aprovacaoId, String eventoId, String error) {
        auditLogPersistenceService.log(
                actor,
                "approval-decision",
                aprovacaoId,
                "failed",
                withDefaults("APROVACAO", aprovacaoId, Map.of(
                        "eventoId", eventoId,
                        "aprovacaoId", aprovacaoId,
                        "error", error)));
    }

    /**
     * Records a pending event creation request.
     */
    public void publishCreatePending(String actor, String aprovacaoId, String organizacaoId) {
        auditLogPersistenceService.log(
                actor,
                "create",
                aprovacaoId,
                "pending",
                withDefaults("APROVACAO", aprovacaoId,
                        Map.of(
                                "solicitacaoAprovacaoId", aprovacaoId,
                                "aprovacaoId", aprovacaoId,
                                "tipoSolicitacao", "CRIACAO_EVENTO",
                                "organizacaoId", organizacaoId)));
    }

    /**
     * Records a successful automatic execution of an event creation.
     */
    public void publishCreateApprovalExecuted(String actor, String aprovacaoId, String eventoId) {
        auditLogPersistenceService.log(
                actor,
                "approval-decision",
                aprovacaoId,
                "executed",
                withDefaults("APROVACAO", aprovacaoId,
                        Map.of("tipoSolicitacao", "CRIACAO_EVENTO", "eventoId", eventoId, "aprovacaoId", aprovacaoId)));
    }

    /**
     * Records a rejected creation request.
     */
    public void publishCreateApprovalRejected(String actor, String aprovacaoId) {
        auditLogPersistenceService.log(
                actor,
                "approval-decision",
                aprovacaoId,
                "rejected",
                withDefaults("APROVACAO", aprovacaoId,
                        Map.of("tipoSolicitacao", "CRIACAO_EVENTO", "aprovacaoId", aprovacaoId)));
    }

    /**
     * Records a failure during automatic creation execution.
     */
    public void publishCreateApprovalFailed(String actor, String aprovacaoId, String error) {
        auditLogPersistenceService.log(
                actor,
                "approval-decision",
                aprovacaoId,
                "failed",
                withDefaults("APROVACAO", aprovacaoId,
                        Map.of("tipoSolicitacao", "CRIACAO_EVENTO", "aprovacaoId", aprovacaoId, "error", error)));
    }

    /**
     * Records a generic approval decision.
     */
    public void publishApprovalDecision(
            String actor,
            AprovacaoEntity aprovacao,
            String result,
            Map<String, Object> additionalMetadata) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("correlationId", aprovacao.getCorrelationId() == null ? "NONE" : aprovacao.getCorrelationId());
        metadata.put("tipoSolicitacao",
                aprovacao.getTipoSolicitacao() == null ? "UNKNOWN" : aprovacao.getTipoSolicitacao());
        metadata.put("solicitacaoAprovacaoId", aprovacao.getId() == null ? "UNKNOWN" : aprovacao.getId().toString());
        metadata.put("aprovacaoId", aprovacao.getId() == null ? "UNKNOWN" : aprovacao.getId().toString());
        metadata.put("eventoId", aprovacao.getEventoId() == null ? "NONE" : aprovacao.getEventoId().toString());
        metadata.putAll(additionalMetadata == null ? Map.of() : additionalMetadata);
        auditLogPersistenceService.log(actor, "approval-decision", aprovacao.getId().toString(), result,
                withDefaults("APROVACAO", aprovacao.getId().toString(), metadata));
    }

    private Map<String, Object> withDefaults(String resourceType, String target, Map<String, Object> metadata) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        resolved.put("resourceType", resourceType);
        if (target != null && !target.isBlank()) {
            resolved.put("resourceId", target);
            if (isUuid(target)) {
                resolved.put("contextId", target);
            }
        }
        String correlationId = MDC.get("correlationId");
        if (correlationId != null && !correlationId.isBlank()) {
            resolved.put("correlationId", correlationId);
        }
        resolved.putAll(metadata == null ? Map.of() : metadata);
        return resolved;
    }

    private boolean isUuid(String value) {
        try {
            java.util.UUID.fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String resolveResourceType(String action) {
        if (action != null && action.startsWith("approval")) {
            return "APROVACAO";
        }
        return "EVENTO";
    }
}
