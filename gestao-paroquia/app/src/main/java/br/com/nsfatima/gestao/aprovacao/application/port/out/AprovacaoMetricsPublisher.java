package br.com.nsfatima.gestao.aprovacao.application.port.out;

import java.time.Duration;

/**
 * Porta de saída (Ports &amp; Adapters) para publicação de métricas do fluxo de aprovação. Isola
 * {@code DecidirSolicitacaoAprovacaoUseCase} do provedor de observabilidade concreto (ex.: Micrometer),
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

    /**
     * Publica o tempo decorrido entre a criação da solicitação e a tomada de decisão (lead time de decisão).
     *
     * @param tipo    tipo da solicitação de aprovação
     * @param status  decisão tomada (ex.: APROVADA, REPROVADA)
     * @param latency tempo decorrido entre abertura e decisão
     */
    void publishAprovacaoDecisionLatency(String tipo, String status, Duration latency);

    /**
     * Publica o tempo total de ponta a ponta decorrido entre a abertura da solicitação e a conclusão da execução.
     *
     * @param tipo    tipo da solicitação de aprovação
     * @param latency tempo total decorrido do ciclo de vida
     */
    void publishAprovacaoTotalLatency(String tipo, Duration latency);

    /**
     * Registra o incremento de uma aprovação deferida no sistema.
     *
     * @param tipo tipo da solicitação de aprovação
     */
    void recordAprovacao(String tipo);

    /**
     * Registra o incremento de uma rejeição de solicitação no sistema.
     *
     * @param tipo tipo da solicitação de aprovação
     */
    void recordRejeicao(String tipo);

    /**
     * Registra o incremento de uma operação de deleção ou cancelamento no sistema.
     *
     * @param recurso tipo do recurso afetado (ex.: EVENTO, OBSERVACAO)
     * @param origem  origem da ação (ex.: APROVACAO, DIRETA)
     */
    void recordDelecao(String recurso, String origem);

    /**
     * Registra o incremento de uma operação de edição ou atualização no sistema.
     *
     * @param recurso tipo do recurso afetado (ex.: EVENTO, OBSERVACAO)
     * @param origem  origem da ação (ex.: APROVACAO, DIRETA)
     */
    void recordEdicao(String recurso, String origem);

    /**
     * Registra a abertura de uma nova solicitação de aprovação pendente no sistema.
     *
     * @param tipo             tipo da solicitação de aprovação
     * @param solicitantePapel papel do solicitante no ato da abertura
     */
    void recordSolicitacaoAberta(String tipo, String solicitantePapel);
}

