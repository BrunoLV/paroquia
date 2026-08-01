package br.com.nsfatima.gestao.patrimonio.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PatrimonioAuditPublisher {

    private static final String TYPE_ATIVO = "ATIVO_PATRIMONIAL";
    private static final String RESULT_SUCCESS = "success";

    private final AuditLogPersistenceService auditLogPersistenceService;

    public PatrimonioAuditPublisher(AuditLogPersistenceService auditLogPersistenceService) {
        this.auditLogPersistenceService = auditLogPersistenceService;
    }

    public void publishCreateSuccess(String actor, UUID ativoId, UUID organizacaoId) {
        auditLogPersistenceService.log(actor, "create", ativoId.toString(), RESULT_SUCCESS,
                withDefaults(ativoId, organizacaoId, Map.of()));
    }

    public void publishTransfer(String actor, UUID ativoId, UUID organizacaoOrigemId, UUID organizacaoDestinoId) {
        auditLogPersistenceService.log(actor, "transferir", ativoId.toString(), RESULT_SUCCESS,
                withDefaults(ativoId, organizacaoDestinoId, Map.of(
                        "organizacaoOrigemId", organizacaoOrigemId.toString(),
                        "organizacaoDestinoId", organizacaoDestinoId.toString())));
    }

    public void publishAlocacao(String actor, UUID ativoId, UUID organizacaoId, UUID eventoId) {
        Map<String, Object> metadata = eventoId == null ? Map.of() : Map.of("eventoId", eventoId.toString());
        auditLogPersistenceService.log(actor, "alocar", ativoId.toString(), RESULT_SUCCESS,
                withDefaults(ativoId, organizacaoId, metadata));
    }

    public void publishDesalocacao(String actor, UUID ativoId, UUID organizacaoId) {
        auditLogPersistenceService.log(actor, "desalocar", ativoId.toString(), RESULT_SUCCESS,
                withDefaults(ativoId, organizacaoId, Map.of()));
    }

    public void publishDeniedWrite(String actor, UUID ativoId) {
        auditLogPersistenceService.log(actor, "write-denied", ativoId == null ? "ativo" : ativoId.toString(),
                "ACCESS_DENIED", Map.of("resourceType", TYPE_ATIVO, "errorCategory", "AUTHZ"));
    }

    private Map<String, Object> withDefaults(UUID ativoId, UUID organizacaoId, Map<String, Object> metadata) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        resolved.put("resourceType", TYPE_ATIVO);
        resolved.put("resourceId", ativoId.toString());
        if (organizacaoId != null) {
            resolved.put("organizacaoId", organizacaoId.toString());
        }
        resolved.putAll(metadata);
        return resolved;
    }
}
