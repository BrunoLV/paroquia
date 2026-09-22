package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoMetricsPublisher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Publica, via log estruturado (SLF4J, com prefixo {@code metric }) e contadores/amostras em
 * memória e Micrometer/Prometheus, as métricas operacionais do fluxo de cadastro/aprovação de eventos do calendário
 * (tempo de cadastro, lead time de registro, latência de consultas de agenda, indicador de
 * retrabalho administrativo, fluxo de cancelamento e fluxo de aprovação). Implementa
 * {@link AprovacaoMetricsPublisher} para que o módulo de aprovação publique essas métricas sem
 * conhecer os detalhes de armazenamento/observabilidade do contexto de calendário. Os contadores e
 * amostras acumulados aqui alimentam o {@link SnapshotMetricasSemanaisJob}, que os agrega em um
 * snapshot semanal para acompanhamento histórico.
 */
@Component
public class CadastroEventoMetricsPublisher implements AprovacaoMetricsPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(CadastroEventoMetricsPublisher.class);
    private static final String TAG_TIPO = "tipo";
    private static final String TAG_STATUS = "status";
    private static final String TAG_RECURSO = "recurso";
    private static final String TAG_ORIGEM = "origem";
    private static final String TAG_DECISAO = "decisao";
    private static final String VALOR_UNKNOWN = "UNKNOWN";


    private final MeterRegistry meterRegistry;
    private final AtomicLong successCount = new AtomicLong();
    private final AtomicLong failureCount = new AtomicLong();
    private final AtomicLong conflictPendenteCount = new AtomicLong();
    private final AtomicLong replayCount = new AtomicLong();
    private final AtomicLong administrativeReworkCount = new AtomicLong();
    private final List<Long> leadTimeMinutesSamples = Collections.synchronizedList(new ArrayList<>());
    private final List<Long> queryLatencyMsSamples = Collections.synchronizedList(new ArrayList<>());
    private final List<Long> aprovacaoExecutionLatencyMsSamples = Collections
            .synchronizedList(new ArrayList<>());

    public CadastroEventoMetricsPublisher() {
        this(new SimpleMeterRegistry());
    }

    @Autowired
    public CadastroEventoMetricsPublisher(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry != null ? meterRegistry : new SimpleMeterRegistry();
    }

    /**
     * Registra o tempo total gasto para processar um cadastro de evento no Micrometer e em log.
     *
     * @param duracao duração medida do processamento do cadastro
     */
    public void publishCadastroTempo(Duration duracao) {
        Timer.builder("gestao.evento.tempo.cadastro")
                .description("Tempo de processamento do cadastro de evento")
                .register(meterRegistry)
                .record(duracao);
        LOGGER.info("metric cadastro_evento_tempo_ms={}", duracao.toMillis());
    }

    /**
     * Registra o lead time entre a solicitação de registro de um evento e sua realização,
     * alimentando percentis e Timer no Micrometer.
     *
     * @param leadTime tempo decorrido entre solicitação e realização do evento
     */
    public void publishEventRegistrationLeadTime(Duration leadTime) {
        long minutes = Math.max(0L, leadTime.toMinutes());
        leadTimeMinutesSamples.add(minutes);
        Timer.builder("gestao.evento.lead_time")
                .description("Lead time entre a solicitação do evento e sua realização")
                .register(meterRegistry)
                .record(leadTime);
        LOGGER.info("metric event_registration_lead_time_minutes value={}", minutes);
    }

    /**
     * Registra a latência de uma consulta ao calendário por endpoint no Micrometer e em log.
     *
     * @param endpoint identificador do endpoint de consulta ao calendário
     * @param duration duração medida da consulta
     */
    public void publishCalendarQueryLatency(String endpoint, Duration duration) {
        long millis = Math.max(0L, duration.toMillis());
        queryLatencyMsSamples.add(millis);
        Timer.builder("gestao.evento.consulta.latencia")
                .description("Latência de consultas à agenda do calendário")
                .tag("endpoint", endpoint != null ? endpoint : "default")
                .register(meterRegistry)
                .record(duration);
        LOGGER.info(
                "metric calendar_query_latency_ms endpoint={} value={} withinTarget={}",
                endpoint,
                millis,
                millis <= 500);
    }

    /**
     * Incrementa e registra o indicador de retrabalho administrativo no Micrometer e em log.
     *
     * @param scheduleChanged indica se houve alteração de data/horário do evento
     * @param cancellation indica se a alteração foi um cancelamento
     * @param responsibleOrgChanged indica se houve troca da organização responsável pelo evento
     */
    public void publishAdministrativeRework(boolean scheduleChanged, boolean cancellation,
                                            boolean responsibleOrgChanged) {
        long count = administrativeReworkCount.incrementAndGet();
        Counter.builder("gestao.evento.retrabalho.total")
                .description("Total de eventos de retrabalho administrativo")
                .tag("horario_alterado", String.valueOf(scheduleChanged))
                .tag("cancelamento", String.valueOf(cancellation))
                .tag("organizacao_alterada", String.valueOf(responsibleOrgChanged))
                .register(meterRegistry)
                .increment();
        LOGGER.info(
                "metric administrative_rework_indicator count={} scheduleChanged={} cancellation={} " +
                        "responsibleOrgChanged={}",
                count,
                scheduleChanged,
                cancellation,
                responsibleOrgChanged);
    }

    /**
     * Registra no Micrometer e em log o modo e o desfecho de um fluxo de cancelamento de evento.
     *
     * @param mode modo/estratégia do cancelamento executado
     * @param outcome desfecho do cancelamento (ex.: sucesso, falha)
     */
    public void publishCancellationFlow(String mode, String outcome) {
        Counter.builder("gestao.evento.cancelamentos.total")
                .description("Total de cancelamentos de eventos")
                .tag("modo", mode != null ? mode : VALOR_UNKNOWN)
                .tag("outcome", outcome != null ? outcome : VALOR_UNKNOWN)
                .register(meterRegistry)
                .increment();
        LOGGER.info("metric cancellation_flow mode={} outcome={}", mode, outcome);
    }



    /**
     * Registra em log e no Micrometer o tipo de solicitação de aprovação e seu desfecho, para acompanhar o
     * comportamento do fluxo de aprovação por tipo de solicitação.
     *
     * @param tipoSolicitacao tipo da solicitação de aprovação processada
     * @param outcome desfecho da solicitação de aprovação
     */
    @Override
    public void publishAprovacaoFlow(String tipoSolicitacao, String outcome) {
        Counter.builder("gestao.aprovacao.flow.total")
                .description("Fluxos de decisão e execução de aprovação")
                .tags(TAG_TIPO, tipoSolicitacao, "outcome", outcome)
                .register(meterRegistry)
                .increment();
        LOGGER.info("metric aprovacao_flow tipoSolicitacao={} outcome={}", tipoSolicitacao, outcome);
    }

    /**
     * Registra uma amostra de latência (em milissegundos) de execução de uma decisão de aprovação,
     * acumulando a amostra para percentil e registrando no Timer do Micrometer.
     *
     * @param tipoSolicitacao tipo da solicitação de aprovação processada
     * @param duration duração medida da execução da decisão de aprovação
     */
    @Override
    public void publishAprovacaoExecutionLatency(String tipoSolicitacao, Duration duration) {
        long millis = Math.max(0L, duration.toMillis());
        aprovacaoExecutionLatencyMsSamples.add(millis);
        Timer.builder("gestao.aprovacao.tempo.execucao")
                .description("Tempo de execução da ação após decisão de aprovação")
                .tags(TAG_TIPO, tipoSolicitacao)
                .register(meterRegistry)
                .record(duration);
        LOGGER.info(
                "metric aprovacao_execution_latency_ms tipoSolicitacao={} value={} withinTarget={} targetMs={}",
                tipoSolicitacao,
                millis,
                millis <= 60000,
                60000);
    }

    /**
     * Registra a latência decorrida entre a abertura da solicitação e a decisão tomada.
     *
     * @param tipo tipo da solicitação de aprovação
     * @param status status da decisão (ex.: APROVADA, REPROVADA)
     * @param latency duração decorrida até a decisão
     */
    @Override
    public void publishAprovacaoDecisionLatency(String tipo, String status, Duration latency) {
        Timer.builder("gestao.aprovacao.tempo.decisao")
                .description("Tempo decorrido entre a criação da solicitação e a decisão")
                .tags(TAG_TIPO, tipo, TAG_STATUS, status)
                .register(meterRegistry)
                .record(latency);
        LOGGER.info("metric aprovacao_decision_latency_ms tipo={} status={} value={}",
                tipo, status, latency.toMillis());
    }

    /**
     * Registra o tempo total decorrido desde a abertura da solicitação até a conclusão da sua execução.
     *
     * @param tipo tipo da solicitação de aprovação
     * @param latency duração total do ciclo de vida
     */
    @Override
    public void publishAprovacaoTotalLatency(String tipo, Duration latency) {
        Timer.builder("gestao.aprovacao.tempo.total")
                .description("Tempo total de ponta a ponta da aprovação e execução")
                .tags(TAG_TIPO, tipo)
                .register(meterRegistry)
                .record(latency);
        LOGGER.info("metric aprovacao_total_latency_ms tipo={} value={}", tipo, latency.toMillis());
    }

    /**
     * Incrementa o contador de solicitações aprovadas no Micrometer.
     *
     * @param tipo tipo da solicitação de aprovação
     */
    @Override
    public void recordAprovacao(String tipo) {
        Counter.builder("gestao.aprovacoes.total")
                .description("Total de solicitações de aprovação aprovadas")
                .tags(TAG_TIPO, tipo)
                .register(meterRegistry)
                .increment();
        Counter.builder("gestao.aprovacao.decisoes.total")
                .description("Total de decisões de aprovação tomadas")
                .tags(TAG_DECISAO, "APROVADA", TAG_TIPO, tipo)
                .register(meterRegistry)
                .increment();
        LOGGER.info("metric aprovacao_aprovada tipo={}", tipo);
    }

    /**
     * Incrementa o contador de solicitações rejeitadas no Micrometer.
     *
     * @param tipo tipo da solicitação de aprovação
     */
    @Override
    public void recordRejeicao(String tipo) {
        Counter.builder("gestao.rejeicoes.total")
                .description("Total de solicitações de aprovação rejeitadas")
                .tags(TAG_TIPO, tipo)
                .register(meterRegistry)
                .increment();
        Counter.builder("gestao.aprovacao.decisoes.total")
                .description("Total de decisões de aprovação tomadas")
                .tags(TAG_DECISAO, "REPROVADA", TAG_TIPO, tipo)
                .register(meterRegistry)
                .increment();
        LOGGER.info("metric aprovacao_rejeitada tipo={}", tipo);
    }

    /**
     * Incrementa o contador de deleções ou cancelamentos executados.
     *
     * @param recurso tipo do recurso afetado
     * @param origem origem da operação (ex.: APROVACAO, DIRETA)
     */
    @Override
    public void recordDelecao(String recurso, String origem) {
        Counter.builder("gestao.delecoes.total")
                .description("Total de operações de deleção ou cancelamento")
                .tags(TAG_RECURSO, recurso, TAG_ORIGEM, origem)
                .register(meterRegistry)
                .increment();
        LOGGER.info("metric delecao_executada recurso={} origem={}", recurso, origem);
    }

    /**
     * Incrementa o contador de edições ou atualizações executadas.
     *
     * @param recurso tipo do recurso afetado
     * @param origem origem da operação (ex.: APROVACAO, DIRETA)
     */
    @Override
    public void recordEdicao(String recurso, String origem) {
        Counter.builder("gestao.edicoes.total")
                .description("Total de operações de edição ou atualização")
                .tags(TAG_RECURSO, recurso, TAG_ORIGEM, origem)
                .register(meterRegistry)
                .increment();
        LOGGER.info("metric edicao_executada recurso={} origem={}", recurso, origem);
    }

    /**
     * Incrementa o contador de solicitações de aprovação abertas no sistema.
     *
     * @param tipo tipo da solicitação de aprovação
     * @param solicitantePapel papel do solicitante no ato da abertura
     */
    @Override
    public void recordSolicitacaoAberta(String tipo, String solicitantePapel) {
        Counter.builder("gestao.aprovacao.solicitadas.total")
                .description("Total de solicitações de aprovação abertas")
                .tags(TAG_TIPO, tipo != null ? tipo : VALOR_UNKNOWN,
                        "papel_solicitante", solicitantePapel != null ? solicitantePapel : VALOR_UNKNOWN)
                .register(meterRegistry)
                .increment();
        LOGGER.info("metric solicitacao_aprovacao_aberta tipo={} papel={}", tipo, solicitantePapel);
    }

    /**
     * Incrementa e registra o sucesso de um cadastro de evento, sinalizando também se ele
     * envolveu conflito com pendência de aprovação e/ou foi um replay (reenvio) de uma solicitação
     * já processada, para acompanhar a saúde do fluxo de criação de eventos.
     *
     * @param conflictPendente indica se o cadastro envolveu conflito com pendência de aprovação
     * @param replay indica se o cadastro foi identificado como um replay de solicitação anterior
     */
    public void publishCreateSuccess(boolean conflictPendente, boolean replay) {
        long success = successCount.incrementAndGet();
        long conflict = conflictPendente ? conflictPendenteCount.incrementAndGet() : conflictPendenteCount.get();
        long replayed = replay ? replayCount.incrementAndGet() : replayCount.get();

        Counter.builder("gestao.evento.criados.total")
                .description("Total de eventos criados no calendário")
                .tag("conflito_pendente", String.valueOf(conflictPendente))
                .tag("replay", String.valueOf(replay))
                .register(meterRegistry)
                .increment();

        if (conflictPendente) {
            Counter.builder("gestao.evento.conflitos.total")
                    .description("Total de conflitos de agenda detectados")
                    .tag("tipo", "PENDENTE_APROVACAO")
                    .register(meterRegistry)
                    .increment();
        }

        if (replay) {
            Counter.builder("gestao.evento.idempotencia.replays.total")
                    .description("Total de reenvios idempotentes de cadastro de eventos")
                    .register(meterRegistry)
                    .increment();
        }

        LOGGER.info(
                "metric cadastro_evento outcome=success successCount={} conflictPendenteCount={} replayCount={}",
                success,
                conflict,
                replayed);
    }

    /**
     * Incrementa e registra a falha de um cadastro de evento, categorizando o motivo da falha para
     * permitir a análise de causas mais frequentes de rejeição no fluxo de criação.
     *
     * @param category categoria/motivo da falha do cadastro
     */
    public void publishCreateFailure(String category) {
        long failure = failureCount.incrementAndGet();
        Counter.builder("gestao.evento.falhas.total")
                .description("Total de falhas ou bloqueios no cadastro de eventos")
                .tag("categoria", category != null ? category : VALOR_UNKNOWN)
                .register(meterRegistry)
                .increment();
        LOGGER.info("metric cadastro_evento outcome=failure category={} failureCount={}", category, failure);
    }



    /**
     * Consolida um instantâneo (snapshot) das métricas acumuladas até o momento: contadores de
     * sucesso, falha, conflito com pendência e replay do cadastro de evento; contador de
     * retrabalho administrativo; e os percentis 95 de lead time de registro, latência de consulta
     * ao calendário e latência de execução de aprovação. Usado pelo
     * {@link SnapshotMetricasSemanaisJob} para compor o relatório semanal de métricas.
     *
     * @return mapa ordenado com os valores agregados de cada métrica
     */
    public Map<String, Object> snapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("cadastroEventoSuccess", successCount.get());
        snapshot.put("cadastroEventoFailure", failureCount.get());
        snapshot.put("cadastroEventoConflictPendente", conflictPendenteCount.get());
        snapshot.put("cadastroEventoReplay", replayCount.get());
        snapshot.put("administrativeReworkCount", administrativeReworkCount.get());
        snapshot.put("eventRegistrationLeadTimeMinutesP95", percentile95(leadTimeMinutesSamples));
        snapshot.put("calendarQueryLatencyMsP95", percentile95(queryLatencyMsSamples));
        snapshot.put("aprovacaoExecutionLatencyMsP95", percentile95(aprovacaoExecutionLatencyMsSamples));
        return snapshot;
    }

    @Override
    public void recordFalhaExecucao(String tipo, String tipoFalha) {
        Counter.builder("aprovacao.falhas.execucao")
                .tag(TAG_TIPO, tipo != null ? tipo : VALOR_UNKNOWN)
                .tag("tipo_falha", tipoFalha != null ? tipoFalha : VALOR_UNKNOWN)
                .description("Total de falhas de execução de aprovação categorizadas por tipo e natureza da falha")
                .register(meterRegistry)
                .increment();
    }

    private long percentile95(List<Long> samples) {
        if (samples.isEmpty()) {
            return 0L;
        }
        List<Long> sorted = new ArrayList<>(samples);
        sorted.sort(Long::compareTo);
        int index = (int) Math.ceil(sorted.size() * 0.95d) - 1;
        return sorted.get(Math.max(0, index));
    }
}
