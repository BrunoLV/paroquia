package br.com.nsfatima.gestao.calendario.integration.eventos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CreateEventoConflitoPendingIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @WithMockUser(roles = "CLERO_PAROCO")
        void shouldCreateWithConflictPendingWhenOverlapExists() throws Exception {
                String firstPayload = """
                                {
                                  "titulo": "Evento Base",
                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000bb",
                                  "inicio": "2026-06-15T10:00:00Z",
                                  "fim": "2026-06-15T11:00:00Z"
                                }
                                """;

                String overlapPayload = """
                                {
                                  "titulo": "Evento Sobreposto",
                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000bb",
                                  "inicio": "2026-06-15T10:30:00Z",
                                  "fim": "2026-06-15T11:30:00Z"
                                }
                                """;

                mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", "evt-conflito-001")
                                .header("X-Actor-Role", "paroco")
                                .header("X-Actor-Org-Type", "CLERO")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000bb")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(firstPayload))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.conflictState").doesNotExist());

                mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", "evt-conflito-002")
                                .header("X-Actor-Role", "paroco")
                                .header("X-Actor-Org-Type", "CLERO")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000bb")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(overlapPayload))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.conflictState").value("CONFLICT_PENDING"));
        }
}
