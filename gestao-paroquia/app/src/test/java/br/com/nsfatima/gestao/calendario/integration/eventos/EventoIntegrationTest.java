package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class EventoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    @DisplayName("Should create event immediately when actor has permission")
    void shouldCreateEventImmediately() throws Exception {
        String orgId = "00000000-0000-0000-0000-000000000001";
        String payload = """
                {
                  "titulo": "Evento Teste",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-12-01T10:00:00Z",
                  "fim": "2026-12-01T12:00:00Z"
                }
                """.formatted(orgId);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Evento Teste"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Should return 400 for invalid interval")
    void shouldFailForInvalidInterval() throws Exception {
        String orgId = "00000000-0000-0000-0000-000000000001";
        String payload = """
                {
                  "titulo": "Evento Invalido",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-12-01T12:00:00Z",
                  "fim": "2026-12-01T10:00:00Z"
                }
                """.formatted(orgId);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("DOMAIN_RULE_VIOLATION"));
    }

    @Test
    @DisplayName("Should cancel event successfully")
    void shouldCancelEvent() throws Exception {
        UUID eventoId = UUID.randomUUID();
        String orgId = "00000000-0000-0000-0000-000000000001";
        
        EventoEntity entity = new EventoEntity();
        entity.setId(eventoId);
        entity.setTitulo("A ser cancelado");
        entity.setOrganizacaoResponsavelId(UUID.fromString(orgId));
        entity.setInicioUtc(Instant.parse("2026-12-01T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-12-01T11:00:00Z"));
        entity.setStatus("CONFIRMADO");
        eventoJpaRepository.save(entity);

        String payload = """
                {
                  "motivo": "Cancelamento de teste"
                }
                """;

        mockMvc.perform(post("/api/v1/eventos/{eventoId}/cancel", eventoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should transition to confirmed when actor has permission")
    void shouldTransitionToConfirmed() throws Exception {
        UUID eventoId = UUID.randomUUID();
        String orgId = "00000000-0000-0000-0000-000000000001";

        EventoEntity entity = new EventoEntity();
        entity.setId(eventoId);
        entity.setTitulo("Draft Event");
        entity.setOrganizacaoResponsavelId(UUID.fromString(orgId));
        entity.setInicioUtc(Instant.parse("2026-12-01T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-12-01T11:00:00Z"));
        entity.setStatus("RASCUNHO");
        eventoJpaRepository.save(entity);

        String payload = """
                {
                  "status": "CONFIRMADO"
                }
                """;

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
                        .header("X-Actor-Role", "coordenador")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));
    }
}
