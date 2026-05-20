package br.com.nsfatima.gestao.observabilidade.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.gestao.observabilidade.domain.exception.PersistenciaAuditoriaObrigatoriaException;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.ExternalMembershipReader;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service responsible for recording auditable operations and security events.
 * Modular version: decoupled from specific business domains.
 */
@Service
public class AuditLogPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogPersistenceService.class);

    private final AuditoriaOperacaoJpaRepository repository;
    private final ObjectMapper objectMapper;

    public AuditLogPersistenceService(
            AuditoriaOperacaoJpaRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void log(String actor, String action, String target, String result) {
        log(actor, action, target, result, Map.of());
    }

    public void log(String actor, String action, String target, String result, Map<String, Object> metadata) {
        Map<String, Object> enriched = enrichMetadata(result, metadata);
        
        LOGGER.info("audit actor={} action={} target={} result={} metadata={}", 
                actor, action, target, result, enriched);

        persistAuditableRecord(actor, action, target, result, enriched);
    }

    private Map<String, Object> enrichMetadata(String result, Map<String, Object> metadata) {
        Map<String, Object> enriched = new LinkedHashMap<>(metadata);
        String correlationId = MDC.get("correlationId");
        if (correlationId != null && !correlationId.isBlank()) {
            enriched.putIfAbsent("correlationId", correlationId);
        }

        String res = String.valueOf(result).toLowerCase();
        if (res.contains("denied") || res.contains("failure")) {
            enriched.putIfAbsent("errorCategory", "SECURITY_OR_VALIDATION");
        }
        return enriched;
    }

    private void persistAuditableRecord(String actor, String action, String target, String result, Map<String, Object> enriched) {
        if (!isAuditableAction(action)) return;

        String resType = resolveResourceType(action, enriched);
        String resId = resolveResourceId(target, enriched, resType);
        UUID contextId = resolveContextId(enriched, resId, resType);
        UUID orgId = resolveOrganizationId(enriched);

        if (resId != null && orgId != null) {
            executeSave(actor, action, result, enriched, resType, resId, contextId, orgId);
        }
    }

    private void executeSave(String actor, String action, String result, Map<String, Object> enriched, 
                            String resType, String resId, UUID contextId, UUID orgId) {
        AuditoriaOperacaoEntity entity = new AuditoriaOperacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizacaoId(orgId);
        entity.setEventoId(contextId);
        entity.setRecursoTipo(resType);
        entity.setRecursoId(resId);
        entity.setAcao(action);
        entity.setResultado(result);
        entity.setAtor(actor == null || actor.isBlank() ? "anonymous" : actor);
        entity.setAtorUsuarioId(resolveActorUserId(enriched));
        entity.setCorrelationId((String) enriched.getOrDefault("correlationId", "n/a"));
        entity.setOcorridoEmUtc(Instant.now());
        entity.setDetalhesAuditaveisJson(writeDetails(enriched));
        
        try {
            repository.save(entity);
        } catch (RuntimeException ex) {
            throw new PersistenciaAuditoriaObrigatoriaException("Persistence failed", ex);
        }
    }

    private boolean isAuditableAction(String action) {
        return action != null && List.of("create", "patch", "cancel", "approval-decision", "approval-decision-request",
                "create-observacao", "update-observacao", "delete-observacao", "system-observacao", "read", "list", "admin-action")
                .contains(action);
    }

    private String resolveResourceType(String action, Map<String, Object> metadata) {
        Object explicit = metadata.get("resourceType");
        if (explicit != null && !String.valueOf(explicit).isBlank()) return String.valueOf(explicit).toUpperCase();
        if (action != null && action.contains("observacao")) return "OBSERVACAO";
        if (action != null && action.startsWith("approval")) return "APROVACAO";
        if (List.of("create", "patch", "cancel").contains(action)) return "EVENTO";
        return "GENERIC";
    }

    private String resolveResourceId(String target, Map<String, Object> metadata, String resourceType) {
        Object explicit = firstNonNull(metadata.get("resourceId"), metadata.get("id"), metadata.get("userId"), metadata.get("organizacaoId"));
        if (explicit != null && !String.valueOf(explicit).isBlank()) return String.valueOf(explicit);
        if (target != null && isUuid(target)) return target;
        return null;
    }

    private UUID resolveContextId(Map<String, Object> metadata, String resId, String resType) {
        UUID contextId = parseUuid(firstNonNull(metadata.get("contextId"), metadata.get("eventoId")));
        if (contextId == null && "EVENTO".equals(resType)) {
            contextId = parseUuid(resId);
        }
        return contextId;
    }

    private UUID resolveOrganizationId(Map<String, Object> metadata) {
        UUID orgId = parseUuid(firstNonNull(metadata.get("organizacaoId"), metadata.get("organizationId")));
        if (orgId != null) return orgId;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetails user) {
            return user.primaryMembership().map(ExternalMembershipReader.Membership::organizationId).orElse(null);
        }
        return null;
    }

    private UUID resolveActorUserId(Map<String, Object> metadata) {
        UUID explicit = parseUuid(firstNonNull(metadata.get("userId"), metadata.get("atorUsuarioId")));
        if (explicit != null) return explicit;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetails user) return user.getUsuarioId();
        return null;
    }

    private String writeDetails(Map<String, Object> enrichedMetadata) {
        try {
            return objectMapper.writeValueAsString(enrichedMetadata);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) return value;
        }
        return null;
    }

    private UUID parseUuid(Object value) {
        if (value == null) return null;
        try {
            return UUID.fromString(String.valueOf(value));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isUuid(String value) {
        return parseUuid(value) != null;
    }
}
