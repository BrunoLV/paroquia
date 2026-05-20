package br.com.nsfatima.gestao.projeto.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.slf4j.MDC;

@Component
public class ProjetoAuditPublisher {

    private final AuditLogPersistenceService auditLogPersistenceService;

    public ProjetoAuditPublisher(AuditLogPersistenceService auditLogPersistenceService) {
        this.auditLogPersistenceService = auditLogPersistenceService;
    }

    public void publish(String actor, String action, String target, String result) {
        auditLogPersistenceService.log(actor, action, target, result, withDefaults(target, Map.of()));
    }

    public void publish(String actor, String action, String target, String result, Map<String, Object> metadata) {
        auditLogPersistenceService.log(actor, action, target, result, withDefaults(target, metadata));
    }

    public void publishCreateSuccess(String actor, String target) {
        auditLogPersistenceService.log(actor, "create", target, "success", withDefaults(target, Map.of()));
    }

    public void publishPatchSuccess(String actor, String target) {
        auditLogPersistenceService.log(actor, "patch", target, "success", withDefaults(target, Map.of()));
    }

    private Map<String, Object> withDefaults(String target, Map<String, Object> metadata) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        resolved.put("resourceType", "PROJETO");
        if (target != null && !target.isBlank()) {
            resolved.put("resourceId", target);
            resolved.put("contextId", target);
        }
        String correlationId = MDC.get("correlationId");
        if (correlationId != null && !correlationId.isBlank()) {
            resolved.put("correlationId", correlationId);
        }
        resolved.putAll(metadata == null ? Map.of() : metadata);
        return resolved;
    }
}
