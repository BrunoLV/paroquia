package br.com.nsfatima.gestao.aprovacao.application.port.out;

import java.time.Duration;

/**
 * Porta de saída (Ports &amp; Adapters) para publicação de métricas do fluxo de aprovação. Isola
 * {@code DecideSolicitacaoAprovacaoUseCase} do provedor de observabilidade concreto (ex.: Micrometer),
 * permitindo acompanhar taxa de aprovações/rejeições/falhas e latência de execução por tipo de solicitação.
 */
public interface AprovacaoMetricsPublisher {

    /**
     * Publica a transição de status do fluxo de aprovação (ex.: EXECUTED, REJECTED, FAILED) para um tipo de
     * solicitação, permitindo dashboards de taxa de aprovação/reprovação/falha por tipo.
     *
     * @param tipo   tipo da solicitação de aprovação
     * @param status status resultante da decisão/execução
     */
    void publishAprovacaoFlow(String tipo, String status);

    /**
     * Publica a latência decorrida entre a decisão de uma aprovação e a conclusão da execução da ação associada.
     *
     * @param tipo    tipo da solicitação de aprovação
     * @param latency tempo decorrido entre decisão e execução
     */
    void publishAprovacaoExecutionLatency(String tipo, Duration latency);
}
