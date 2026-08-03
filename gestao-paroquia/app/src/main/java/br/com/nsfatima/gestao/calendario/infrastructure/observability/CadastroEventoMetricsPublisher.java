package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoMetricsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CadastroEventoMetricsPublisher implements AprovacaoMetricsPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(CadastroEventoMetricsPublisher.class);

    private final AtomicLong successCount = new AtomicLong();
    private final AtomicLong failureCount = new AtomicLong();
    private final AtomicLong conflictPendenteCount = new AtomicLong();
    private final AtomicLong replayCount = new AtomicLong();
    private final AtomicLong administrativeReworkCount = new AtomicLong();
    private final List<Long> leadTimeMinutesSamples = Collections.synchronizedList(new ArrayList<>());
    private final List<Long> queryLatencyMsSamples = Collections.synchronizedList(new ArrayList<>());
    private final List<Long> aprovacaoExecutionLatencyMsSamples = Collections
            .synchronizedList(new ArrayList<>());

    public void publishCadastroTempo(Duration duracao) {
        LOGGER.info("metric cadastro_evento_tempo_ms={}", duracao.toMillis());
    }

    public void publishEventRegistrationLeadTime(Duration leadTime) {
        long minutes = Math.max(0L, leadTime.toMinutes());
        leadTimeMinutesSamples.add(minutes);
        LOGGER.info("metric event_registration_lead_time_minutes value={}", minutes);
    }

    public void publishCalendarQueryLatency(String endpoint, Duration duration) {
        long millis = Math.max(0L, duration.toMillis());
        queryLatencyMsSamples.add(millis);
        LOGGER.info(
                "metric calendar_query_latency_ms endpoint={} value={} withinTarget={}",
                endpoint,
                millis,
                millis <= 500);
    }

    public void publishAdministrativeRework(boolean scheduleChanged, boolean cancellation,
                                            boolean responsibleOrgChanged) {
        long count = administrativeReworkCount.incrementAndGet();
        LOGGER.info(
                "metric administrative_rework_indicator count={} scheduleChanged={} cancellation={} " +
                        "responsibleOrgChanged={}",
                count,
                scheduleChanged,
                cancellation,
                responsibleOrgChanged);
    }

    public void publishCancellationFlow(String mode, String outcome) {
        LOGGER.info("metric cancellation_flow mode={} outcome={}", mode, outcome);
    }

    public void publishAprovacaoFlow(String tipoSolicitacao, String outcome) {
        LOGGER.info("metric aprovacao_flow tipoSolicitacao={} outcome={}", tipoSolicitacao, outcome);
    }

    public void publishAprovacaoExecutionLatency(String tipoSolicitacao, Duration duration) {
        long millis = Math.max(0L, duration.toMillis());
        aprovacaoExecutionLatencyMsSamples.add(millis);
        LOGGER.info(
                "metric aprovacao_execution_latency_ms tipoSolicitacao={} value={} withinTarget={} targetMs={}",
                tipoSolicitacao,
                millis,
                millis <= 60000,
                60000);
    }

    public void publishCreateSuccess(boolean conflictPendente, boolean replay) {
        long success = successCount.incrementAndGet();
        long conflict = conflictPendente ? conflictPendenteCount.incrementAndGet() : conflictPendenteCount.get();
        long replayed = replay ? replayCount.incrementAndGet() : replayCount.get();
        LOGGER.info(
                "metric cadastro_evento outcome=success successCount={} conflictPendenteCount={} replayCount={}",
                success,
                conflict,
                replayed);
    }

    public void publishCreateFailure(String category) {
        long failure = failureCount.incrementAndGet();
        LOGGER.info("metric cadastro_evento outcome=failure category={} failureCount={}", category, failure);
    }

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
