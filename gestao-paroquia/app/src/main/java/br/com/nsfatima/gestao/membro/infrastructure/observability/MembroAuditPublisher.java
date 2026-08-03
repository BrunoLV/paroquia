package br.com.nsfatima.gestao.membro.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditoriaPersistenceService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MembroAuditPublisher {

    private final AuditoriaPersistenceService auditLogService;

    public MembroAuditPublisher(AuditoriaPersistenceService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public void publish(String actor, String action, String resourceId, String result) {
        auditLogService.log(actor, action, resourceId, result, Map.of("resourceType", "MEMBRO"));
    }
}
