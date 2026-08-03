package br.com.nsfatima.gestao.projeto.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaPersistenceService;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publica registros de auditoria para ações realizadas sobre projetos (criação, atualização,
 * transições de estado), sempre marcando {@code resourceType} como {@code PROJETO} e propagando
 * o {@code correlationId} da requisição corrente (MDC), seguindo o padrão de publisher de
 * auditoria adotado pelos demais contextos em vez de logging ad hoc.
 */
@Component
public class ProjetoAuditPublisher {

    private final AuditoriaPersistenceService auditLogPersistenceService;

    public ProjetoAuditPublisher(AuditoriaPersistenceService auditLogPersistenceService) {
        this.auditLogPersistenceService = auditLogPersistenceService;
    }

    /**
     * Publica um registro de auditoria genérico para uma ação sobre projeto, sem metadados
     * adicionais além dos padrões (tipo de recurso, ID do recurso, correlationId).
     *
     * @param actor  identificador do ator que realizou a ação.
     * @param action nome da ação realizada.
     * @param target identificador do projeto/recurso alvo da ação.
     * @param result resultado da ação (ex.: {@code "success"}, {@code "failure"}).
     */
    public void publish(String actor, String action, String target, String result) {
        auditLogPersistenceService.log(actor, action, target, result, withDefaults(target, Map.of()));
    }

    /**
     * Publica um registro de auditoria para uma ação sobre projeto, incluindo metadados
     * adicionais além dos padrões.
     *
     * @param actor    identificador do ator que realizou a ação.
     * @param action   nome da ação realizada.
     * @param target   identificador do projeto/recurso alvo da ação.
     * @param result   resultado da ação (ex.: {@code "success"}, {@code "failure"}).
     * @param metadata metadados adicionais a incluir no registro, mesclados com os padrões.
     */
    public void publish(String actor, String action, String target, String result, Map<String, Object> metadata) {
        auditLogPersistenceService.log(actor, action, target, result, withDefaults(target, metadata));
    }

    /**
     * Atalho para publicar um registro de sucesso na criação de um projeto.
     *
     * @param actor  identificador do ator que criou o projeto.
     * @param target identificador do projeto criado.
     */
    public void publishCreateSuccess(String actor, String target) {
        auditLogPersistenceService.log(actor, "criar", target, "success", withDefaults(target, Map.of()));
    }

    /**
     * Atalho para publicar um registro de sucesso na atualização parcial (patch) de um projeto.
     *
     * @param actor  identificador do ator que atualizou o projeto.
     * @param target identificador do projeto atualizado.
     */
    public void publishPatchSuccess(String actor, String target) {
        auditLogPersistenceService.log(actor, "atualizar", target, "success", withDefaults(target, Map.of()));
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
