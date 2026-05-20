package br.com.nsfatima.gestao.calendario.performance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.support.SecurityTestSupport;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class UpdateEventoTier1PerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void shouldKeepPatchLatencyWithinCiOperationalGuardrail() throws Exception {
        UUID eventoId = UUID.randomUUID();

        EventoEntity entity = new EventoEntity();
        entity.setId(eventoId);
        entity.setTitulo("Evento base performance patch");
        entity.setDescricao("medicao tier 1 patch");
        entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        entity.setInicioUtc(Instant.parse("2026-12-01T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-12-01T11:00:00Z"));
        entity.setStatus("CONFIRMADO");
        eventoJpaRepository.save(entity);

        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "joao.silva", "senha123");

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          \"titulo\": \"Evento atualizado warmup\"
                        }
                        """))
                .andExpect(status().isOk());

        long startNanos = System.nanoTime();
        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          \"titulo\": \"Evento atualizado em benchmark\"
                        }
                        """))
                .andExpect(status().isOk());
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;

        assertThat(elapsedMillis)
                .as("PATCH valido deve manter latencia Tier-1 dentro de limite operacional de CI")
                .isLessThanOrEqualTo(5000L);
    }
}
