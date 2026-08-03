package br.com.nsfatima.gestao.calendario.integration.metrics;

import br.com.nsfatima.gestao.calendario.application.service.CalculadoraTaxaRetrabalho;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.SnapshotMetricasSemanaisJob;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class WeeklyMetricsSnapshotIntegrationTest {

    @Test
    void deveProduzirSemanalSnapshot() {
        AuditoriaOperacaoJpaRepository auditRepository = mock(AuditoriaOperacaoJpaRepository.class);
        when(auditRepository.findByOcorridoEmUtcGreaterThanEqualAndOcorridoEmUtcLessThanOrderByOcorridoEmUtcAscIdAsc(
                any(),
                any()))
                .thenReturn(List.of());

        SnapshotMetricasSemanaisJob job = new SnapshotMetricasSemanaisJob(
                new CadastroEventoMetricsPublisher(),
                new PeriodoOperacionalPolicy(),
                new CalculadoraTaxaRetrabalho(auditRepository, new ObjectMapper()));
        assertEquals("ok", job.snapshot().get("status"));
        assertNotNull(job.snapshot().get("cadastroEvento"));
        assertNotNull(job.snapshot().get("administrativeRework"));
    }
}
