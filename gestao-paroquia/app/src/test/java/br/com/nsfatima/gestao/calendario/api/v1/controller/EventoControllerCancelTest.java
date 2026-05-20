package br.com.nsfatima.gestao.calendario.api.v1.controller;

import java.time.Instant;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventoControllerCancelTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    void shouldCancelEventWithPost() throws Exception {
        UUID eventId = UUID.randomUUID();
        createEvento(eventId, UUID.fromString("00000000-0000-0000-0000-000000000001"));

        String payload = """
                {
                  "motivo": "Cancelamento resiliente"
                }
                """;

        mockMvc.perform(post("/api/v1/eventos/" + eventId + "/cancel")
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());
    }

    private void createEvento(UUID id, UUID orgId) {
        EventoEntity entity = new EventoEntity();
        entity.setId(id);
        entity.setTitulo("Evento para cancelar");
        entity.setOrganizacaoResponsavelId(orgId);
        entity.setInicioUtc(Instant.parse("2026-05-15T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-05-15T11:00:00Z"));
        entity.setStatus("CONFIRMADO");
        eventoJpaRepository.save(entity);
    }
}
