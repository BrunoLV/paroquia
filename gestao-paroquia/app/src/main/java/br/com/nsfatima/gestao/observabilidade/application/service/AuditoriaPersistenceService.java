package br.com.nsfatima.gestao.observabilidade.application.service;

import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.observabilidade.domain.exception.PersistenciaAuditoriaObrigatoriaException;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço central de auditoria: recebe eventos de negócio de qualquer contexto (calendário,
 * projeto, aprovação, patrimônio etc.) e decide, com base em um vocabulário fechado de ações
 * ("acao"), se e como persistir um registro auditável em {@link AuditoriaOperacaoEntity}.
 *
 * <p>Vocabulário de "acao" atualmente aceito (ver {@link #isAuditableAction(String)}): "criar",
 * "atualizar", "cancelar", "aprovacao-decisao", "aprovacao-decisao-solicitacao",
 * "criar-observacao", "atualizar-observacao", "excluir-observacao", "sistema-observacao", "ler",
 * "listar", "acao-admin", "transferir", "alocar". Ações fora dessa lista são logadas mas não
 * persistidas — chamadores de outros contextos devem reutilizar esses literais (nunca inventar
 * novos) para que o registro seja efetivamente auditado.
 *
 * <p>Convenções de chaves no {@code metadata}/JSON de detalhes que este serviço reconhece:
 * "usuarioId" (ator autenticado), "grupoParoquialId" (escopo organizacional obrigatório para
 * persistir), "resourceType"/"resourceId"/"id", "contextId"/"eventoId" e "correlationId"
 * (propagado via MDC, ver {@link CorrelationIdFilter}). Um registro só é persistido quando o
 * identificador do recurso e a organização puderem ser resolvidos; caso contrário, o evento
 * fica apenas no log estruturado.
 */
@Service
public class AuditoriaPersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditoriaPersistenceService.class);
    private static final String KEY_CORRELATION_ID = "correlationId";
    private final AuditoriaOperacaoJpaRepository repository;
    private final ObjectMapper objectMapper;

    public AuditoriaPersistenceService(
            AuditoriaOperacaoJpaRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Registra uma operação auditável sem metadados adicionais. Equivalente a chamar a
     * sobrecarga completa com um mapa vazio — útil quando o recurso auditado é identificável
     * apenas por {@code target} (ex.: um UUID) e não há contexto extra a propagar.
     *
     * @param actor identificador do ator que executou a ação (login/nome); "anonymous" se nulo/vazio
     * @param action ação executada; deve pertencer ao vocabulário fechado documentado na classe
     * @param target identificador do recurso afetado (tipicamente um UUID)
     * @param result resultado da operação (ex.: "sucesso", "falha", "denied")
     */
    public void log(String actor, String action, String target, String result) {
        log(actor, action, target, result, Map.of());
    }

    /**
     * Registra uma operação auditável, sempre emitindo uma linha de log estruturado e, quando a
     * ação pertence ao vocabulário auditável e for possível resolver organização e recurso,
     * persistindo o registro em {@link AuditoriaOperacaoEntity} (ver
     * {@link #persistAuditableRecord}). O {@code metadata} é enriquecido com o correlationId
     * corrente (MDC) e uma categoria de erro quando o resultado indica falha/negação.
     *
     * @param actor identificador do ator que executou a ação; "anonymous" se nulo/vazio
     * @param action ação executada; deve pertencer ao vocabulário fechado documentado na classe
     * @param target identificador do recurso afetado, usado como fallback de resourceId
     * @param result resultado da operação (ex.: "sucesso", "falha", "denied")
     * @param metadata contexto adicional; chaves reconhecidas incluem "usuarioId",
     *     "grupoParoquialId", "resourceType", "resourceId"/"id", "contextId"/"eventoId"
     */
    public void log(String actor, String action, String target, String result, Map<String, Object> metadata) {
        Map<String, Object> enriched = enrichMetadata(result, metadata);

        LOGGER.info("audit actor={} action={} target={} result={} metadata={}",
                actor, action, target, result, enriched);

        persistAuditableRecord(actor, action, target, result, enriched);
    }

    private Map<String, Object> enrichMetadata(String result, Map<String, Object> metadata) {
        Map<String, Object> enriched = new LinkedHashMap<>(metadata);
        String correlationId = MDC.get(KEY_CORRELATION_ID);
        if (correlationId != null && !correlationId.isBlank()) {
            enriched.putIfAbsent(KEY_CORRELATION_ID, correlationId);
        }

        String res = String.valueOf(result).toLowerCase();
        if (res.contains("denied") || res.contains("failure")) {
            enriched.putIfAbsent("errorCategory", "SECURITY_OR_VALIDATION");
        }
        return enriched;
    }

    private void persistAuditableRecord(String actor,
                                        String action, String target, String result, Map<String, Object> enriched) {

        if (!isAuditableAction(action)) {
            return;
        }

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
        entity.setGrupoParoquialId(orgId);
        entity.setEventoId(contextId);
        entity.setRecursoTipo(resType);
        entity.setRecursoId(resId);
        entity.setAcao(action);
        entity.setResultado(result);
        entity.setAtor(actor == null || actor.isBlank() ? "anonymous" : actor);
        entity.setAtorUsuarioId(resolveActorUserId(enriched));
        entity.setCorrelationId((String) enriched.getOrDefault(KEY_CORRELATION_ID, "n/a"));
        entity.setOcorridoEmUtc(Instant.now());
        entity.setDetalhesAuditaveisJson(writeDetails(enriched));

        try {
            repository.save(entity);
        } catch (RuntimeException ex) {
            throw new PersistenciaAuditoriaObrigatoriaException("Persistence failed", ex);
        }
    }

    private boolean isAuditableAction(String action) {
        return action != null && List.of("criar", "atualizar", "cancelar",
                        "aprovacao-decisao", "aprovacao-decisao-solicitacao",
                        "criar-observacao",
                        "atualizar-observacao", "excluir-observacao", "sistema-observacao", "ler", "listar",
                        "acao-admin", "transferir", "alocar")

                .contains(action);
    }

    private String resolveResourceType(String action, Map<String, Object> metadata) {
        Object explicit = metadata.get("resourceType");
        if (explicit != null && !String.valueOf(explicit).isBlank()) {
            return String.valueOf(explicit).toUpperCase();
        }
        if (action != null && action.contains("observacao")) {
            return "OBSERVACAO";
        }
        if (action != null && action.startsWith("aprovacao")) {
            return "APROVACAO";
        }
        if (List.of("criar", "atualizar", "cancelar").contains(action)) {
            return "EVENTO";
        }
        return "GENERIC";
    }

    private String resolveResourceId(String target, Map<String, Object> metadata, String resourceType) {
        Object explicit = firstNonNull(
                metadata.get("resourceId"), metadata.get("id"),
                metadata.get("usuarioId"), metadata.get("grupoParoquialId"));
        if (explicit != null && !String.valueOf(explicit).isBlank()) {
            return String.valueOf(explicit);
        }
        if (target != null && isUuid(target)) {
            return target;
        }
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
        UUID orgId = parseUuid(metadata.get("grupoParoquialId"));
        if (orgId != null) {
            return orgId;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetails user) {
            return user.vinculoPrimario().map(LeitorVinculoExterno.Vinculo::organizationId).orElse(null);
        }
        return null;
    }

    private UUID resolveActorUserId(Map<String, Object> metadata) {
        UUID explicit = parseUuid(firstNonNull(metadata.get("usuarioId"), metadata.get("atorUsuarioId")));
        if (explicit != null) {
            return explicit;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetails user) {
            return user.getUsuarioId();
        }
        return null;
    }

    private String writeDetails(Map<String, Object> enrichedMetadata) {
        try {
            return objectMapper.writeValueAsString(enrichedMetadata);
        } catch (JacksonException ex) {
            return "{}";
        }
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private UUID parseUuid(Object value) {
        if (value == null) {
            return null;
        }
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
