package br.com.nsfatima.gestao.calendario.integration.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.nsfatima.gestao.calendario.application.usecase.metrics.ReworkRateCalculator;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.WeeklyMetricsSnapshotJob;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WeeklyMetricsSnapshotHistoryIntegrationTest {

    private AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;

    private WeeklyMetricsSnapshotJob snapshotJob;

    @BeforeEach
    void setUp() {
        auditoriaOperacaoJpaRepository = mock(AuditoriaOperacaoJpaRepository.class);
        when(auditoriaOperacaoJpaRepository
                .findByOcorridoEmUtcGreaterThanEqualAndOcorridoEmUtcLessThanOrderByOcorridoEmUtcAscIdAsc(
                        any(),
                        any()))
                .thenReturn(List.of(record(
                        UUID.fromString("00000000-0000-0000-0000-0000000000aa"),
                        UUID.randomUUID(),
                        "patch",
                        "{\"scheduleChanged\":true}")));
        snapshotJob = new WeeklyMetricsSnapshotJob(
                new CadastroEventoMetricsPublisher(),
                new PeriodoOperacionalPolicy(),
                new ReworkRateCalculator(auditoriaOperacaoJpaRepository, new ObjectMapper()));
    }

    @Test
    void shouldRetainHistoricalBaselineAcrossConsecutiveSnapshots() {
        Map<String, Object> first = snapshotJob.snapshot();
        Map<String, Object> second = snapshotJob.snapshot();

        assertThat(snapshotJob.history()).hasSize(2);
        assertThat(first).containsKey("administrativeRework");
        assertThat(second).containsEntry("historySize", 2);
    }

    private AuditoriaOperacaoEntity record(UUID organizacaoId, UUID eventoId, String acao, String detalhes) {
        AuditoriaOperacaoEntity entity = new AuditoriaOperacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizacaoId(organizacaoId);
        entity.setEventoId(eventoId);
        entity.setRecursoTipo("EVENTO");
        entity.setRecursoId(eventoId.toString());
        entity.setAcao(acao);
        entity.setResultado("success");
        entity.setAtor("tester");
        entity.setCorrelationId("corr-history");
        entity.setDetalhesAuditaveisJson(detalhes);
        entity.setOcorridoEmUtc(Instant.now());
        return entity;
    }
}
