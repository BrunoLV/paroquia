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

    @Override
    public void publishAprovacaoDecisionLatency(String tipo, String status, Duration latency) {
        calls.add(new MetricCall("aprovacao_decision_latency", List.of(tipo, status, latency)));
    }

    @Override
    public void publishAprovacaoTotalLatency(String tipo, Duration latency) {
        calls.add(new MetricCall("aprovacao_total_latency", List.of(tipo, latency)));
    }

    @Override
    public void recordAprovacao(String tipo) {
        calls.add(new MetricCall("record_aprovacao", List.of(tipo)));
    }

    @Override
    public void recordRejeicao(String tipo) {
        calls.add(new MetricCall("record_rejeicao", List.of(tipo)));
    }

    @Override
    public void recordDelecao(String recurso, String origem) {
        calls.add(new MetricCall("record_delecao", List.of(recurso, origem)));
    }

    @Override
    public void recordEdicao(String recurso, String origem) {
        calls.add(new MetricCall("record_edicao", List.of(recurso, origem)));
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
