package br.com.nsfatima.gestao.aprovacao.application.usecase;

import java.time.Duration;

public interface ApprovalMetricsPublisher {
    void publishApprovalFlow(String tipo, String status);
    void publishApprovalExecutionLatency(String tipo, Duration latency);
}
