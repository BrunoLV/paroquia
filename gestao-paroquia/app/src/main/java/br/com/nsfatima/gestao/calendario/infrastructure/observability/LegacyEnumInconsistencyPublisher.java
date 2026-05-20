package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LegacyEnumInconsistencyPublisher {

    private final AuditLogPersistenceService auditLogPersistenceService;

    public LegacyEnumInconsistencyPublisher(AuditLogPersistenceService auditLogPersistenceService) {
        this.auditLogPersistenceService = auditLogPersistenceService;
    }

    public void publish(String aggregateType, String aggregateId, String field, String rawValue) {
        auditLogPersistenceService.log(
                "system",
                "legacy-enum-inconsistency",
                "%s:%s".formatted(aggregateType, aggregateId),
                "detected",
                Map.of(
                        "field", field,
                        "rawValue", sanitize(rawValue)));
    }

    private String sanitize(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "<empty>";
        }

        String normalized = rawValue.replaceAll("\\s+", " ").trim();
        return normalized.length() > 80 ? normalized.substring(0, 80) : normalized;
    }
}
