package br.com.nsfatima.gestao.calendario.infrastructure.observability;

import br.com.nsfatima.gestao.calendario.application.service.CalculadoraTaxaRetrabalho;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metricas.PeriodoOperacionalResponse;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Job agendado que, toda segunda-feira à 01:00 UTC, captura um instantâneo (snapshot) consolidado
 * das métricas semanais do contexto de calendário — resultado agregado do
 * {@link CadastroEventoMetricsPublisher} (sucesso/falha de cadastro, retrabalho administrativo,
 * percentis de lead time e latência) e a taxa de retrabalho calculada pela
 * {@link CalculadoraTaxaRetrabalho} para o período semanal resolvido pela
 * {@link PeriodoOperacionalPolicy} — e acumula esses snapshots em memória para permitir
 * acompanhamento de tendência histórica (relatórios semanais, comparação período a período) sem
 * depender de reprocessar os dados brutos a cada consulta.
 */
@Component
public class SnapshotMetricasSemanaisJob {

    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;
    private final PeriodoOperacionalPolicy periodoOperacionalPolicy;
    private final CalculadoraTaxaRetrabalho calculadoraTaxaRetrabalho;
    private final List<Map<String, Object>> snapshotHistory = Collections.synchronizedList(new ArrayList<>());

    public SnapshotMetricasSemanaisJob(
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher,
            PeriodoOperacionalPolicy periodoOperacionalPolicy,
            CalculadoraTaxaRetrabalho calculadoraTaxaRetrabalho) {
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
        this.periodoOperacionalPolicy = periodoOperacionalPolicy;
        this.calculadoraTaxaRetrabalho = calculadoraTaxaRetrabalho;
    }

    /**
     * Executa a captura do snapshot semanal de métricas, agendada via cron para toda segunda-feira
     * às 01:00 UTC (logo após o encerramento da semana anterior). Resolve o período semanal
     * corrente, calcula a taxa de retrabalho para esse período, obtém o snapshot corrente das
     * métricas de cadastro de evento, monta um mapa com o instante de captura, o status e ambos os
     * conjuntos de métricas, e acrescenta uma cópia imutável desse mapa ao histórico em memória
     * antes de retorná-lo (com o tamanho do histórico anexado).
     *
     * @return mapa com o snapshot capturado: instante de captura, status, métricas de cadastro de
     *     evento, métricas de retrabalho administrativo do período e tamanho do histórico
     *     acumulado
     */
    @Scheduled(cron = "0 0 1 * * MON", zone = "UTC")
    public Map<String, Object> snapshot() {
        PeriodoOperacionalPolicy.ResolvedPeriod periodo = periodoOperacionalPolicy.resolve("SEMANAL", null, null);
        CalculadoraTaxaRetrabalho.MetricasRetrabalhoSnapshot reworkMetrics =
                calculadoraTaxaRetrabalho.calcularParaSnapshot(periodo);
        Map<String, Object> cadastroMetrics = cadastroEventoMetricsPublisher.snapshot();
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("capturedAt", Instant.now().toString());
        snapshot.put("status", "ok");
        snapshot.put("cadastroEvento", cadastroMetrics);
        snapshot.put("administrativeRework", Map.of(
                "periodo", new PeriodoOperacionalResponse(periodo.granularidade(), periodo.inicio(), periodo.fim()),
                "taxaRetrabalho", reworkMetrics.taxaRetrabalho(),
                "numeradorOcorrenciasElegiveis", reworkMetrics.numeradorOcorrenciasElegiveis(),
                "denominadorEventosAfetados", reworkMetrics.denominadorEventosAfetados()));
        snapshotHistory.add(Map.copyOf(snapshot));
        snapshot.put("historySize", snapshotHistory.size());
        return snapshot;
    }

    /**
     * Retorna o histórico completo de snapshots semanais já capturados por este job, em ordem de
     * captura, para consulta de tendência histórica das métricas.
     *
     * @return cópia imutável da lista de snapshots acumulados
     */
    public List<Map<String, Object>> history() {
        return List.copyOf(snapshotHistory);
    }
}
