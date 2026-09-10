package br.com.nsfatima.gestao.membro.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Publica eventos de auditoria para as mutações do contexto {@code membro} (criação, atualização,
 * ativação/desativação e inscrição em organização), delegando ao serviço central de auditoria com o
 * {@code resourceType} fixo {@code "MEMBRO"} para permitir rastreabilidade e correlação de ações sobre este
 * agregado.
 */
@Component
public class MembroAuditPublisher {

    private final AuditoriaPersistenceService auditLogService;

    public MembroAuditPublisher(AuditoriaPersistenceService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Registra uma ação de auditoria sobre um recurso do contexto {@code membro}.
     *
     * @param actor      identificador do agente que executou a ação
     * @param action     nome da ação realizada (ex.: {@code "criar"}, {@code "atualizar"}, {@code "ativar"})
     * @param resourceId identificador do recurso afetado
     * @param result     resultado da operação (ex.: {@code "sucesso"})
     */
    public void publish(String actor, String action, String resourceId, String result) {
        auditLogService.log(actor, action, resourceId, result, Map.of("resourceType", "MEMBRO"));
    }
}
