package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class EventoAuditPublisher implements AprovacaoAuditPublisher {

    /**
     * Publishes an audit log entry with additional metadata.
     * <p>
     * Usage Example:
     * publisher.publish("admin", "atualizar", "evento:123", "success", Map.of("fields", List.of("titulo")));
     */
    private static final String KEY_RESOURCE_TYPE = "resourceType";
    private static final String TYPE_EVENTO = "EVENTO";
    private static final String TYPE_APROVACAO = "APROVACAO";
    private static final String ACTION_CREATE = "criar";
    private static final String ACTION_APROVACAO_DECISION = "aprovacao-decisao";
    private static final String KEY_SOLICITACAO_APROVACAO_ID = "solicitacaoAprovacaoId";
    private static final String KEY_APROVACAO_ID = "aprovacaoId";
    private static final String KEY_EVENTO_ID = "eventoId";
    private static final String KEY_TIPO_SOLICITACAO = "tipoSolicitacao";
    private static final String VAL_CRIACAO_EVENTO = "CRIACAO_EVENTO";
    private static final String VAL_NONE = "NONE";
    private static final String VAL_UNKNOWN = "UNKNOWN";
    private static final String KEY_CORRELATION_ID = "correlationId";
    private final AuditoriaPersistenceService auditLogPersistenceService;
    public EventoAuditPublisher(AuditoriaPersistenceService auditLogPersistenceService) {
        this.auditLogPersistenceService = auditLogPersistenceService;
    }

    /**
     * Publishes a simple audit log entry.
     * <p>
     * Usage Example:
     * publisher.publish("admin", "ler", "evento:123", "success");
     */
    public void publish(String actor, String action, String target, String result) {
        auditLogPersistenceService.log(actor,
                action, target, result, withDefaults(resolveResourceType(action), target, Map.of()));

    }

    public void publish(String actor, String action, String target, String result, Map<String, Object> metadata) {
        String resourceType = metadata != null && metadata.get(KEY_RESOURCE_TYPE) != null
                ? String.valueOf(metadata.get(KEY_RESOURCE_TYPE))
                : resolveResourceType(action);
        auditLogPersistenceService.log(actor, action, target, result, withDefaults(resourceType, target, metadata));
    }

    /**
     * Records a successful event creation.
     */
    public void publishCreateSuccess(String actor, String target, boolean replay, String conflictState) {
        auditLogPersistenceService.log(
                actor,
                ACTION_CREATE,
                target,
                "success",
                withDefaults(TYPE_EVENTO, target, Map.of(
                        "replay", replay,
                        "conflictState", conflictState == null ? VAL_NONE : conflictState)));
    }

    /**
     * Records a failed event creation.
     */
    public void publishCreateFailure(String actor, String errorCategory, String message) {
        auditLogPersistenceService.log(
                actor,
                ACTION_CREATE,
                "evento",
                "failure",
                withDefaults(TYPE_EVENTO, "evento", Map.of(
                        "errorCategory", errorCategory,
                        "message", message)));
    }

    /**
     * Records a successful event listing.
     */
    public void publishListSuccess(String actor, int totalItems) {
        auditLogPersistenceService.log(actor, "listar", "eventos", "success", Map.of("totalItems", totalItems));
    }

    /**
     * Records a denied write attempt.
     */
    public void publishDeniedWrite(String actor, String target) {
        auditLogPersistenceService.log(actor,
                "escrita-negada", target, "ACCESS_DENIED", Map.of("errorCategory", "AUTHZ"));

    }

    /**
     * Records a pendente event cancellation request.
     */
    public void publishCancellationPendente(String actor, String eventoId, String solicitacaoAprovacaoId,
                                           String motivo) {
        auditLogPersistenceService.log(
                actor,
                "cancelar",
                eventoId,
                "pendente",
                withDefaults(TYPE_EVENTO, eventoId, Map.of(
                        KEY_SOLICITACAO_APROVACAO_ID, solicitacaoAprovacaoId,
                        "canceladoMotivo", motivo)));
    }

    /**
     * Records a rejected cancellation request.
     */
    public void publishCancellationRejected(String actor, String aprovacaoId, String eventoId) {
        auditLogPersistenceService.log(
                actor,
                ACTION_APROVACAO_DECISION,
                aprovacaoId,
                "rejected",
                withDefaults(TYPE_APROVACAO, aprovacaoId,
                        Map.of(KEY_EVENTO_ID, eventoId, KEY_APROVACAO_ID, aprovacaoId)));
    }

    /**
     * Records a successful automatic execution of a cancellation.
     */
    public void publishCancellationExecuted(String actor, String aprovacaoId, String eventoId) {
        auditLogPersistenceService.log(
                actor,
                ACTION_APROVACAO_DECISION,
                aprovacaoId,
                "executed",
                withDefaults(TYPE_APROVACAO, aprovacaoId,
                        Map.of(KEY_EVENTO_ID, eventoId, KEY_APROVACAO_ID, aprovacaoId)));
    }

    /**
     * Records a failure during automatic cancellation execution.
     */
    public void publishCancellationExecutionFailed(String actor, String aprovacaoId, String eventoId, String error) {
        auditLogPersistenceService.log(
                actor,
                ACTION_APROVACAO_DECISION,
                aprovacaoId,
                "failed",
                withDefaults(TYPE_APROVACAO, aprovacaoId, Map.of(
                        KEY_EVENTO_ID, eventoId,
                        KEY_APROVACAO_ID, aprovacaoId,
                        "error", error)));
    }

    /**
     * Records a pendente event creation request.
     */
    public void publishCreatePendente(String actor, String aprovacaoId, String grupoParoquialId) {
        auditLogPersistenceService.log(
                actor,
                ACTION_CREATE,
                aprovacaoId,
                "pendente",
                withDefaults(TYPE_APROVACAO, aprovacaoId,
                        Map.of(
                                KEY_SOLICITACAO_APROVACAO_ID, aprovacaoId,
                                KEY_APROVACAO_ID, aprovacaoId,
                                KEY_TIPO_SOLICITACAO, VAL_CRIACAO_EVENTO,
                                "grupoParoquialId", grupoParoquialId)));
    }

    /**
     * Records a successful automatic execution of an event creation.
     */
    public void publishCreateAprovacaoExecuted(String actor, String aprovacaoId, String eventoId) {
        auditLogPersistenceService.log(
                actor,
                ACTION_APROVACAO_DECISION,
                aprovacaoId,
                "executed",
                withDefaults(TYPE_APROVACAO, aprovacaoId,
                        Map.of(KEY_TIPO_SOLICITACAO, VAL_CRIACAO_EVENTO,
                                KEY_EVENTO_ID, eventoId, KEY_APROVACAO_ID, aprovacaoId)));
    }

    /**
     * Records a rejected creation request.
     */
    public void publishCreateAprovacaoRejected(String actor, String aprovacaoId) {
        auditLogPersistenceService.log(
                actor,
                ACTION_APROVACAO_DECISION,
                aprovacaoId,
                "rejected",
                withDefaults(TYPE_APROVACAO, aprovacaoId,
                        Map.of(KEY_TIPO_SOLICITACAO, VAL_CRIACAO_EVENTO, KEY_APROVACAO_ID, aprovacaoId)));
    }

    /**
     * Records a failure during automatic creation execution.
     */
    public void publishCreateAprovacaoFailed(String actor, String aprovacaoId, String error) {
        auditLogPersistenceService.log(
                actor,
                ACTION_APROVACAO_DECISION,
                aprovacaoId,
                "failed",
                withDefaults(TYPE_APROVACAO, aprovacaoId,
                        Map.of(KEY_TIPO_SOLICITACAO, VAL_CRIACAO_EVENTO,
                                KEY_APROVACAO_ID, aprovacaoId, "error", error)));
    }

    /**
     * Records a generic aprovacao decision.
     */
    public void publishAprovacaoDecision(
            String actor,
            AprovacaoEntity aprovacao,
            String result,
            Map<String, Object> additionalMetadata) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put(KEY_CORRELATION_ID, aprovacao.getCorrelationId() == null
                ? VAL_NONE : aprovacao.getCorrelationId());
        metadata.put(KEY_TIPO_SOLICITACAO,
                aprovacao.getTipoSolicitacao() == null ? VAL_UNKNOWN : aprovacao.getTipoSolicitacao());
        metadata.put(KEY_SOLICITACAO_APROVACAO_ID, aprovacao.getId() == null
                ? VAL_UNKNOWN : aprovacao.getId().toString());
        metadata.put(KEY_APROVACAO_ID, aprovacao.getId() == null
                ? VAL_UNKNOWN : aprovacao.getId().toString());
        metadata.put(KEY_EVENTO_ID, aprovacao.getEventoId() == null
                ? VAL_NONE : aprovacao.getEventoId().toString());
        metadata.putAll(additionalMetadata == null ? Map.of() : additionalMetadata);
        auditLogPersistenceService.log(actor, ACTION_APROVACAO_DECISION, aprovacao.getId().toString(), result,
                withDefaults(TYPE_APROVACAO, aprovacao.getId().toString(), metadata));
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
            UUID.fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String resolveResourceType(String action) {
        if (action != null && action.startsWith("aprovacao")) {
            return "APROVACAO";
        }
        return "EVENTO";
    }
}
