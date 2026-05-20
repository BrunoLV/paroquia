package br.com.nsfatima.gestao.calendario.support.fake;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Fake implementation of CadastroEventoMetricsPublisher that records calls for assertion.
 */
public class FakeCadastroEventoMetricsPublisher extends CadastroEventoMetricsPublisher {

    public record MetricCall(String name, Object value) {}
    
    private final List<MetricCall> calls = new ArrayList<>();

    @Override
    public void publishCreateSuccess(boolean conflictPending, boolean replay) {
        calls.add(new MetricCall("create_success", List.of(conflictPending, replay)));
    }

    @Override
    public void publishApprovalFlow(String tipo, String outcome) {
        calls.add(new MetricCall("approval_flow", List.of(tipo, outcome)));
    }

    @Override
    public void publishApprovalExecutionLatency(String tipo, Duration latency) {
        calls.add(new MetricCall("approval_execution_latency", List.of(tipo, latency)));
    }

    public List<MetricCall> getCalls() {
        return List.copyOf(calls);
    }
    
    public void clear() {
        calls.clear();
    }
}
