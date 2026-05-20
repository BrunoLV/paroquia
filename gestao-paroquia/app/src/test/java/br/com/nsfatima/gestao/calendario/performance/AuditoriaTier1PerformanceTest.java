package br.com.nsfatima.gestao.calendario.performance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuditoriaTier1PerformanceTest {

    private static final long P95_LIMIT_MS = 2000L;
    private static final int SAMPLE_SIZE = 10;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;

    private UUID organizacaoId;

    @BeforeEach
    void setUp() {
        auditoriaOperacaoJpaRepository.deleteAll();
        organizacaoId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        for (int index = 0; index < 25; index++) {
            auditoriaOperacaoJpaRepository.save(record(organizacaoId, UUID.randomUUID(), index));
        }
    }

    @Test
    void shouldKeepTrailAndReworkQueriesWithinTier1Threshold() throws Exception {
        List<Long> trilhaLatencies = new ArrayList<>();
        List<Long> retrabalhoLatencies = new ArrayList<>();

        for (int sample = 0; sample < SAMPLE_SIZE; sample++) {
            long trilhaStart = System.nanoTime();
            mockMvc.perform(get("/api/v1/auditoria/eventos/trilha")
                    .param("organizacaoId", organizacaoId.toString())
                    .param("inicio", "2026-10-01T00:00:00Z")
                    .param("fim", "2026-10-10T00:00:00Z")
                    .header("X-Actor-Org-Id", organizacaoId.toString()))
                    .andExpect(status().isOk());
            trilhaLatencies.add(toMillis(System.nanoTime() - trilhaStart));

            long retrabalhoStart = System.nanoTime();
            mockMvc.perform(get("/api/v1/auditoria/eventos/retrabalho")
                    .param("organizacaoId", organizacaoId.toString())
                    .param("inicio", "2026-10-01T00:00:00Z")
                    .param("fim", "2026-10-10T00:00:00Z")
                    .header("X-Actor-Org-Id", organizacaoId.toString()))
                    .andExpect(status().isOk());
            retrabalhoLatencies.add(toMillis(System.nanoTime() - retrabalhoStart));
        }

        assertThat(p95(trilhaLatencies)).isLessThanOrEqualTo(P95_LIMIT_MS);
        assertThat(p95(retrabalhoLatencies)).isLessThanOrEqualTo(P95_LIMIT_MS);
    }

    private AuditoriaOperacaoEntity record(UUID orgId, UUID eventoId, int index) {
        AuditoriaOperacaoEntity entity = new AuditoriaOperacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizacaoId(orgId);
        entity.setEventoId(eventoId);
        entity.setRecursoTipo("EVENTO");
        entity.setRecursoId(eventoId.toString());
        entity.setAcao(index % 2 == 0 ? "patch" : "cancel");
        entity.setResultado("success");
        entity.setAtor("perf");
        entity.setCorrelationId("corr-perf-" + index);
        entity.setDetalhesAuditaveisJson(index % 2 == 0 ? "{\"scheduleChanged\":true}" : "{}");
        entity.setOcorridoEmUtc(Instant.parse("2026-10-03T10:15:00Z"));
        return entity;
    }

    private long toMillis(long nanos) {
        return nanos / 1_000_000L;
    }

    private long p95(List<Long> values) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil(0.95 * sorted.size()) - 1;
        return sorted.get(Math.max(index, 0));
    }
}
