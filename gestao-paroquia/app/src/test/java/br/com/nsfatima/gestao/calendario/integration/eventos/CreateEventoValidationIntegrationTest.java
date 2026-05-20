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
class CreateEventoValidationIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @WithMockUser
        void shouldRejectCreateWhenEndIsBeforeStart() throws Exception {
                String payload = """
                                {
                                  "titulo": "Evento Invalido",
                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                  "inicio": "2026-06-11T18:00:00Z",
                                  "fim": "2026-06-11T17:00:00Z"
                                }
                                """;

                mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", "evt-validation-create-001")
                                .header("X-Actor-Role", "paroco")
                                .header("X-Actor-Org-Type", "CLERO")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(payload))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors[0].code").value("DOMAIN_RULE_VIOLATION"));
        }

        @Test
        @WithMockUser
        void shouldRejectAddedExtraWithoutJustification() throws Exception {
                String payload = """
                                {
                                  "titulo": "Evento Extra",
                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                  "inicio": "2026-06-11T17:00:00Z",
                                  "fim": "2026-06-11T18:00:00Z",
                                  "status": "ADICIONADO_EXTRA"
                                }
                                """;

                mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", "evt-validation-create-002")
                                .header("X-Actor-Role", "paroco")
                                .header("X-Actor-Org-Type", "CLERO")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(payload))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.errors[0].code").value("DOMAIN_RULE_VIOLATION"));
        }
}
