package br.com.nsfatima.gestao.calendario.support.fake;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fake implementation of EventoAuditPublisher that records published events for assertion.
 */
public class FakeEventoAuditPublisher extends EventoAuditPublisher {

    public record AuditEvent(String actor, String action, String target, String result, Map<String, Object> metadata) {}
    
    private final List<AuditEvent> events = new ArrayList<>();

    public FakeEventoAuditPublisher() {
        super(null); // No real service needed
    }

    @Override
    public void publish(String actor, String action, String target, String result, Map<String, Object> metadata) {
        events.add(new AuditEvent(actor, action, target, result, metadata));
    }

    @Override
    public void publishCreateSuccess(String actor, String eventoId, boolean replayed, String conflictState) {
        publish(actor, "create", eventoId, "success", Map.of("replayed", replayed, "conflictState", conflictState));
    }

    @Override
    public void publishApprovalDecision(String actor, AprovacaoEntity aprovacao, String result, Map<String, Object> metadata) {
        publish(actor, "approval-decision", aprovacao.getId().toString(), result, metadata);
    }

    public List<AuditEvent> getEvents() {
        return List.copyOf(events);
    }
    
    public void clear() {
        events.clear();
    }
}
