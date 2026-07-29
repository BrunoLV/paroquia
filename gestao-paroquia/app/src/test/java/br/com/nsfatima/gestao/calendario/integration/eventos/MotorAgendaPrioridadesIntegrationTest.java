package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MotorAgendaPrioridadesIntegrationTest {

    private static final String ORG_ID = "00000000-0000-0000-0000-0000000000aa";
    private static final String IGREJA_ID = "00000000-0000-0000-0000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @AfterEach
    void tearDown() {
        eventoJpaRepository.deleteAll();
    }

    @Test
    void shouldRejectLevel4WhenConflictWithLevel2() throws Exception {
        // 1. Create Level 2 Event (LITURGICO)
        String level2Payload = """
                {
                  "titulo": "Missa Dominical",
                  "categoria": "LITURGICO",
                  "organizacaoResponsavelId": "%s",
                  "igrejaVinculoId": "%s",
                  "inicio": "2026-08-10T10:00:00Z",
                  "fim": "2026-08-10T11:30:00Z"
                }
                """.formatted(ORG_ID, IGREJA_ID);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "evt-prio-001")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(level2Payload))
                .andExpect(status().isCreated());

        // 2. Attempt Level 4 Event (MOVIMENTO) at same time and church
        String level4Payload = """
                {
                  "titulo": "Reunião de Jovens",
                  "categoria": "MOVIMENTO",
                  "organizacaoResponsavelId": "%s",
                  "igrejaVinculoId": "%s",
                  "inicio": "2026-08-10T10:30:00Z",
                  "fim": "2026-08-10T11:30:00Z"
                }
                """.formatted(ORG_ID, IGREJA_ID);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "evt-prio-002")
                        .header("X-Actor-Role", "coord_movimento")
                        .header("X-Actor-Org-Type", "MOVIMENTO")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(level4Payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("CONFLITO_AGENDA_ECLESIAL"));
    }

    @Test
    void shouldAllowLevel4WhenParocoUsesBypass() throws Exception {
        // 1. Create Level 2 Event (LITURGICO)
        String level2Payload = """
                {
                  "titulo": "Missa Solene",
                  "categoria": "LITURGICO",
                  "organizacaoResponsavelId": "%s",
                  "igrejaVinculoId": "%s",
                  "inicio": "2026-08-11T10:00:00Z",
                  "fim": "2026-08-11T11:30:00Z"
                }
                """.formatted(ORG_ID, IGREJA_ID);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "evt-prio-003")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(level2Payload))
                .andExpect(status().isCreated());

        // 2. Paroco creates Level 4 Event with bypass
        String level4BypassPayload = """
                {
                  "titulo": "Encontro de Casais Especial",
                  "categoria": "MOVIMENTO",
                  "organizacaoResponsavelId": "%s",
                  "igrejaVinculoId": "%s",
                  "inicio": "2026-08-11T10:30:00Z",
                  "fim": "2026-08-11T11:30:00Z",
                  "bypassAgendaConflict": true
                }
                """.formatted(ORG_ID, IGREJA_ID);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "evt-prio-004")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(level4BypassPayload))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldRejectBypassWhenUserIsNotParoco() throws Exception {
        String bypassPayload = """
                {
                  "titulo": "Tentativa de Bypass sem permissão",
                  "categoria": "PASTORAL",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-08-12T10:00:00Z",
                  "fim": "2026-08-12T11:00:00Z",
                  "bypassAgendaConflict": true
                }
                """.formatted(ORG_ID);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "evt-prio-005")
                        .header("X-Actor-Role", "coord_pastoral_local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(bypassPayload))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAutoCancelLowerPriorityEventsWhenLevel1Created() throws Exception {
        // 1. Create Level 3 Event (PASTORAL)
        String level3Payload = """
                {
                  "titulo": "Reunião Pastoral Catequética",
                  "categoria": "PASTORAL",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-08-15T10:00:00Z",
                  "fim": "2026-08-15T12:00:00Z"
                }
                """.formatted(ORG_ID);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "evt-prio-006")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(level3Payload))
                .andExpect(status().isCreated());

        EventoEntity pastoralEvent = eventoJpaRepository.findAll().stream()
                .filter(e -> "Reunião Pastoral Catequética".equals(e.getTitulo()))
                .findFirst()
                .orElseThrow();
        assertEquals("RASCUNHO", pastoralEvent.getStatus());

        // 2. Create Level 1 Event (SOLENIDADE) overlapping same period
        String level1Payload = """
                {
                  "titulo": "Festa da Padroeira (Solenidade)",
                  "categoria": "SOLENIDADE",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-08-15T09:00:00Z",
                  "fim": "2026-08-15T18:00:00Z"
                }
                """.formatted(ORG_ID);

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "evt-prio-007")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(level1Payload))
                .andExpect(status().isCreated());

        // 3. Check that Pastoral Event status was automatically set to CANCELADO
        EventoEntity updatedPastoralEvent = eventoJpaRepository.findById(pastoralEvent.getId()).orElseThrow();
        assertEquals("CANCELADO", updatedPastoralEvent.getStatus());
    }
}
