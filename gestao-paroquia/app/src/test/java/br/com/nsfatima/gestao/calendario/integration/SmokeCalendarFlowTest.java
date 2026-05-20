package br.com.nsfatima.gestao.calendario.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SmokeCalendarFlowTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void shouldRunBasicCalendarJourney() throws Exception {
                mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", "evt-smoke-flow-001")
                                .header("X-Actor-Role", "paroco")
                                .header("X-Actor-Org-Type", "CLERO")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000dd")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(
                                                "{\"titulo\":\"Evento Smoke\",\"organizacaoResponsavelId\":\"00000000-0000-0000-0000-0000000000dd\",\"inicio\":\"2026-03-16T10:00:00Z\",\"fim\":\"2026-03-16T11:00:00Z\"}"))
                                .andExpect(status().isCreated());

                mockMvc.perform(post("/api/v1/eventos/{eventoId}/observacoes", "00000000-0000-0000-0000-000000000001")
                                .header("X-Actor-Role", "paroco")
                                .header("X-Actor-Org-Type", "CLERO")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000dd")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(
                                                "{\"tipo\":\"nota\",\"conteudo\":\"Fluxo smoke\"}"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.tipo").value("NOTA"));

                mockMvc.perform(get("/api/v1/eventos"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[?(@.titulo=='Evento Smoke')]").exists());
        }
}
