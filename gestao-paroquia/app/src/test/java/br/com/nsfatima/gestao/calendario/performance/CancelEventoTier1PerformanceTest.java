package br.com.nsfatima.gestao.calendario.performance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CancelEventoTier1PerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    void shouldCompleteDirectCancellationWithinTwoSeconds() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity entity = new EventoEntity();
        entity.setId(eventoId);
        entity.setTitulo("Cancelamento performance");
        entity.setDescricao("medicao tier 1");
        entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        entity.setInicioUtc(Instant.parse("2026-12-01T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-12-01T11:00:00Z"));
        entity.setStatus("CONFIRMADO");
        eventoJpaRepository.save(entity);

        long startNanos = System.nanoTime();
        mockMvc.perform(delete("/api/v1/eventos/{eventoId}", eventoId)
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          \"motivo\": \"Medição de latência Tier 1\"
                        }
                        """))
                .andExpect(status().isOk());
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;

        assertThat(elapsedMillis)
                .as("Cancelamento direto deve completar em até 2000ms")
                .isLessThanOrEqualTo(2000L);
    }
}
