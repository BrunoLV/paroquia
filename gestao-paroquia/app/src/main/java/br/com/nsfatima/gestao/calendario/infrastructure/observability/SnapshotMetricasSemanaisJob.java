package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import br.com.nsfatima.gestao.calendario.application.service.CalculadoraTaxaRetrabalho;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SnapshotMetricasSemanaisJob {

    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;
    private final PeriodoOperacionalPolicy periodoOperacionalPolicy;
    private final CalculadoraTaxaRetrabalho reworkRateCalculator;
    private final List<Map<String, Object>> snapshotHistory = Collections.synchronizedList(new ArrayList<>());

    public SnapshotMetricasSemanaisJob(
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher,
            PeriodoOperacionalPolicy periodoOperacionalPolicy,
            CalculadoraTaxaRetrabalho reworkRateCalculator) {
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
        this.periodoOperacionalPolicy = periodoOperacionalPolicy;
        this.reworkRateCalculator = reworkRateCalculator;
    }

    @Scheduled(cron = "0 0 1 * * MON", zone = "UTC")
    public Map<String, Object> snapshot() {
        PeriodoOperacionalPolicy.ResolvedPeriod periodo = periodoOperacionalPolicy.resolve("SEMANAL", null, null);
        CalculadoraTaxaRetrabalho.SnapshotReworkMetrics reworkMetrics = reworkRateCalculator.calculateForSnapshot(periodo);
        Map<String, Object> cadastroMetrics = cadastroEventoMetricsPublisher.snapshot();
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("capturedAt", Instant.now().toString());
        snapshot.put("status", "ok");
        snapshot.put("cadastroEvento", cadastroMetrics);
        snapshot.put("administrativeRework", Map.of(
                "periodo", periodo.toResponse(),
                "taxaRetrabalho", reworkMetrics.taxaRetrabalho(),
                "numeradorOcorrenciasElegiveis", reworkMetrics.numeradorOcorrenciasElegiveis(),
                "denominadorEventosAfetados", reworkMetrics.denominadorEventosAfetados()));
        snapshotHistory.add(Map.copyOf(snapshot));
        snapshot.put("historySize", snapshotHistory.size());
        return snapshot;
    }

    public List<Map<String, Object>> history() {
        return List.copyOf(snapshotHistory);
    }
}
