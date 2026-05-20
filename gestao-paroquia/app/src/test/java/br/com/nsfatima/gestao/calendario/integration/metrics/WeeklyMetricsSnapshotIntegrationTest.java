package br.com.nsfatima.gestao.calendario.integration.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.ReworkRateCalculator;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.WeeklyMetricsSnapshotJob;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WeeklyMetricsSnapshotIntegrationTest {

    @Test
    void shouldProduceWeeklySnapshot() {
        AuditoriaOperacaoJpaRepository auditRepository = mock(AuditoriaOperacaoJpaRepository.class);
        when(auditRepository.findByOcorridoEmUtcGreaterThanEqualAndOcorridoEmUtcLessThanOrderByOcorridoEmUtcAscIdAsc(
                any(),
                any()))
                .thenReturn(java.util.List.of());

        WeeklyMetricsSnapshotJob job = new WeeklyMetricsSnapshotJob(
                new CadastroEventoMetricsPublisher(),
                new PeriodoOperacionalPolicy(),
                new ReworkRateCalculator(auditRepository, new ObjectMapper()));
        assertEquals("ok", job.snapshot().get("status"));
        assertNotNull(job.snapshot().get("cadastroEvento"));
        assertNotNull(job.snapshot().get("administrativeRework"));
    }
}
