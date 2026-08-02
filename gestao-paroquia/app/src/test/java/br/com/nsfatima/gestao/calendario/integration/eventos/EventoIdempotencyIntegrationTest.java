package br.com.nsfatima.gestao.calendario.integration.eventos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class EventoIdempotencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "CLERO_PAROCO")
    void deveReplayMesmaResponseParaMesmaIdempotenciaChaveEPayload() throws Exception {
        String payload = """
                {
                  "titulo": "Assembleia",
                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                  "inicio": "2026-08-01T09:00:00Z",
                  "fim": "2026-08-01T11:00:00Z"
                }
                """;

        MvcResult firstResult = mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "evt-idempotency-001")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult secondResult = mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "evt-idempotency-001")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode firstBody = objectMapper.readTree(firstResult.getResponse().getContentAsString());
        JsonNode secondBody = objectMapper.readTree(secondResult.getResponse().getContentAsString());

        assertEquals(firstBody.get("id").asString(), secondBody.get("id").asString());
    }

    @Test
    @WithMockUser(roles = "CLERO_PAROCO")
    void deveRetornarConflitoQuandoIdempotenciaChaveEReusedComDiferentePayload() throws Exception {
        String firstPayload = """
                {
                  "titulo": "Assembleia",
                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                  "inicio": "2026-08-01T09:00:00Z",
                  "fim": "2026-08-01T11:00:00Z"
                }
                """;

        String secondPayload = """
                {
                  "titulo": "Assembleia Alterada",
                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                  "inicio": "2026-08-01T09:00:00Z",
                  "fim": "2026-08-01T11:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "evt-idempotency-002")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(firstPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "evt-idempotency-002")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(secondPayload))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0].code").value("IDEMPOTENCY_KEY_CONFLICT"));
    }
}
