package br.com.nsfatima.gestao.calendario.support.fake;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Fake implementation of CadastroEventoMetricsPublisher that records calls for assertion.
 */
public class FakeCadastroEventoMetricsPublisher extends CadastroEventoMetricsPublisher {

    private final List<MetricCall> calls = new ArrayList<>();

    @Override
    public void publishCreateSuccess(boolean conflictPendente, boolean replay) {
        calls.add(new MetricCall("create_success", List.of(conflictPendente, replay)));
    }

    @Override
    public void publishAprovacaoFlow(String tipo, String outcome) {
        calls.add(new MetricCall("aprovacao_flow", List.of(tipo, outcome)));
    }

    @Override
    public void publishAprovacaoExecutionLatency(String tipo, Duration latency) {
        calls.add(new MetricCall("aprovacao_execution_latency", List.of(tipo, latency)));
    }

    public List<MetricCall> getCalls() {
        return List.copyOf(calls);
    }

    public void clear() {
        calls.clear();
    }

    public record MetricCall(String name, Object value) {
    }
}
