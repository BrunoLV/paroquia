package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publica, via {@link AuditoriaPersistenceService}, os eventos de auditoria relativos a
 * "observações" registradas sobre eventos do calendário (anotações administrativas associadas a
 * um evento), usando um vocabulário fixo de ações que deve casar com o vocabulário fechado
 * aceito por {@code AuditoriaPersistenceService.isAuditableAction}: {@code criar-observacao},
 * {@code atualizar-observacao}, {@code excluir-observacao}, {@code sistema-observacao} e
 * {@code listar} (emitida por {@link #publishList}, reaproveitando a mesma ação genérica de
 * listagem usada pelos demais publishers, não um sufixo próprio) são todas ações auditáveis
 * (persistidas em {@code AuditoriaOperacaoEntity} além de logadas). Todos os métodos enriquecem os
 * metadados publicados com {@code resourceType=OBSERVACAO}, o correlationId corrente (via MDC) e,
 * quando o alvo é informado, {@code targetEventId}/{@code contextId} apontando para o evento
 * relacionado.
 */
@Component
public class ObservacaoAuditPublisher {

    private final AuditoriaPersistenceService auditLogPersistenceService;

    public ObservacaoAuditPublisher(AuditoriaPersistenceService auditLogPersistenceService) {
        this.auditLogPersistenceService = auditLogPersistenceService;
    }

    /**
     * Publica um evento de auditoria genérico sobre uma observação, com a ação informada pelo
     * chamador e sem metadados adicionais além dos defaults (resourceType, correlationId e
     * alvo/evento relacionado).
     *
     * @param actor identificador do ator que executou a ação
     * @param action ação executada (deve pertencer ao vocabulário fechado de
     *     {@code AuditoriaPersistenceService} para ser efetivamente persistida)
     * @param target identificador do evento/observação afetado
     * @param result resultado da operação (ex.: "sucesso", "falha")
     */
    public void publish(String actor, String action, String target, String result) {
        auditLogPersistenceService.log(actor, action, target, result, withDefaults(target, Map.of()));
    }

    /**
     * Publica o evento de auditoria de criação de uma observação (ação
     * {@code criar-observacao}, persistida).
     *
     * @param actor identificador do ator que criou a observação
     * @param target identificador do evento/observação afetado
     * @param result resultado da criação
     * @param metadata metadados adicionais da operação, mesclados com os defaults
     */
    public void publishCreate(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogPersistenceService.log(actor, "criar-observacao", target, result, withDefaults(target, metadata));
    }

    /**
     * Publica o evento de auditoria de atualização de uma observação (ação
     * {@code atualizar-observacao}, persistida).
     *
     * @param actor identificador do ator que atualizou a observação
     * @param target identificador do evento/observação afetado
     * @param result resultado da atualização
     * @param metadata metadados adicionais da operação, mesclados com os defaults
     */
    public void publishUpdate(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogPersistenceService.log(actor, "atualizar-observacao", target, result, withDefaults(target, metadata));
    }

    /**
     * Publica o evento de auditoria de exclusão de uma observação (ação
     * {@code excluir-observacao}, persistida).
     *
     * @param actor identificador do ator que excluiu a observação
     * @param target identificador do evento/observação afetado
     * @param result resultado da exclusão
     * @param metadata metadados adicionais da operação, mesclados com os defaults
     */
    public void publishDelete(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogPersistenceService.log(actor, "excluir-observacao", target, result, withDefaults(target, metadata));
    }

    /**
     * Publica o evento de auditoria de listagem de observações (ação {@code listar}, persistida).
     *
     * @param actor identificador do ator que executou a listagem
     * @param target identificador do evento relacionado à listagem, quando aplicável
     * @param result resultado da listagem
     * @param metadata metadados adicionais da operação, mesclados com os defaults
     */
    public void publishList(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogPersistenceService.log(actor, "listar", target, result, withDefaults(target, metadata));
    }

    /**
     * Publica o evento de auditoria de uma ação de sistema sobre uma observação (ação
     * {@code sistema-observacao}, persistida), usado para operações automáticas/internas que não
     * partem diretamente de uma ação de criação, atualização, exclusão ou listagem por um usuário.
     *
     * @param actor identificador do ator (tipicamente "system") que executou a ação
     * @param target identificador do evento/observação afetado
     * @param result resultado da ação de sistema
     * @param metadata metadados adicionais da operação, mesclados com os defaults
     */
    public void publishSystem(String actor, String target, String result, Map<String, Object> metadata) {
        auditLogPersistenceService.log(actor, "sistema-observacao", target, result, withDefaults(target, metadata));
    }

    private Map<String, Object> withDefaults(String target, Map<String, Object> metadata) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        resolved.put("resourceType", "OBSERVACAO");
        String correlationId = MDC.get("correlationId");
        if (correlationId != null && !correlationId.isBlank()) {
            resolved.put("correlationId", correlationId);
        }
        if (target != null && !target.isBlank()) {
            resolved.put("targetEventId", target);
            resolved.put("contextId", target);
        }
        resolved.putAll(metadata == null ? Map.of() : metadata);
        return resolved;
    }
}
