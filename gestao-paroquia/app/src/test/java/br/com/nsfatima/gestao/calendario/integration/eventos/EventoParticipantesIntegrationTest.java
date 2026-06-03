package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.type.PapelEnvolvido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class EventoParticipantesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private EventoEnvolvidoJpaRepository involvementRepository;

    @Test
    @DisplayName("Should return participants in response when creating event")
    void shouldReturnParticipantsInResponse() throws Exception {
        String orgId = "00000000-0000-0000-0000-0000000000cc"; // Conselho C
        String participanteId = "00000000-0000-0000-0000-0000000000bb"; // Pastoral B
        String payload = """
                {
                  "titulo": "Evento com Participantes",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-12-01T10:00:00Z",
                  "fim": "2026-12-01T12:00:00Z",
                  "participantes": ["%s"]
                }
                """.formatted(orgId, participanteId);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .header("X-Actor-Role", "coordenador")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participantes").isArray())
                .andExpect(jsonPath("$.participantes[0]").value(participanteId));
    }

    @Test
    @DisplayName("Should return updated participants in response")
    void shouldReturnUpdatedParticipantsInResponse() throws Exception {
        UUID eventoId = UUID.randomUUID();
        String orgId = "00000000-0000-0000-0000-0000000000aa";
        String initialParticipanteId = "00000000-0000-0000-0000-0000000000bb";
        String newParticipanteId = "00000000-0000-0000-0000-0000000000cc";

        // 1. Create event with initial participant
        EventoEntity entity = new EventoEntity();
        entity.setId(eventoId);
        entity.setTitulo("Event for Update Participants");
        entity.setOrganizacaoResponsavelId(UUID.fromString(orgId));
        entity.setInicioUtc(Instant.parse("2026-12-01T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-12-01T11:00:00Z"));
        entity.setStatus("CONFIRMADO");
        eventoJpaRepository.save(entity);

        // Add initial involvement
        EventoEnvolvidoEntity inv = new EventoEnvolvidoEntity();
        inv.setEventoId(eventoId);
        inv.setOrganizacaoId(UUID.fromString(initialParticipanteId));
        inv.setPapelParticipacao(PapelEnvolvido.APOIO);
        involvementRepository.save(inv);
        
        // 2. Perform update
        String payload = """
                {
                  "participantes": ["%s"]
                }
                """.formatted(newParticipanteId);

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
                        .header("X-Actor-Role", "coordenador")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantes").isArray())
                .andExpect(jsonPath("$.participantes[0]").value(newParticipanteId));
    }
}
