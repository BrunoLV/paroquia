package br.com.nsfatima.gestao.calendario.performance;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ObservacaoTier1PerformanceTest {

    private static final long P95_LIMIT_MS = 2000L;
    private static final int SAMPLE_SIZE = 10;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @BeforeEach
    void setUp() {
        observacaoEventoJpaRepository.deleteAll();
    }

    @Test
    void shouldKeepP95BelowTier1ThresholdForCreateEditList() throws Exception {
        String eventoId = "00000000-0000-0000-0000-0000000000aa";

        List<Long> createLatencies = new ArrayList<>();
        List<Long> editLatencies = new ArrayList<>();
        List<Long> listLatencies = new ArrayList<>();

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            long createStart = System.nanoTime();
            MvcResult createResult = mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", eventoId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"tipo\":\"NOTA\",\"conteudo\":\"Perf note " + i + "\"}"))
                    .andExpect(status().isCreated())
                    .andReturn();
            createLatencies.add(nanosToMillis(System.nanoTime() - createStart));

            JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
            UUID observacaoId = UUID.fromString(created.path("id").asText());

            long editStart = System.nanoTime();
            mockMvc.perform(patch("/api/v1/eventos/{eventoId}/observacoes/{observacaoId}", eventoId, observacaoId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"conteudo\":\"Perf edit " + i + "\"}"))
                    .andExpect(status().isOk());
            editLatencies.add(nanosToMillis(System.nanoTime() - editStart));

            long listStart = System.nanoTime();
            mockMvc.perform(get("/api/v1/eventos/{eventoId}/observacoes", eventoId))
                    .andExpect(status().isOk());
            listLatencies.add(nanosToMillis(System.nanoTime() - listStart));
        }

        assertThat(p95(createLatencies)).isLessThanOrEqualTo(P95_LIMIT_MS);
        assertThat(p95(editLatencies)).isLessThanOrEqualTo(P95_LIMIT_MS);
        assertThat(p95(listLatencies)).isLessThanOrEqualTo(P95_LIMIT_MS);
    }

    private long nanosToMillis(long nanos) {
        return nanos / 1_000_000L;
    }

    private long p95(List<Long> values) {
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil(0.95 * sorted.size()) - 1;
        return sorted.get(Math.max(index, 0));
    }
}
