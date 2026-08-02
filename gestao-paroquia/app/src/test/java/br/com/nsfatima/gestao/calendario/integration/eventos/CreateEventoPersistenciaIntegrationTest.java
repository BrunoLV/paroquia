package br.com.nsfatima.gestao.calendario.integration.eventos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class CreateEventoPersistenciaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "CLERO_PAROCO")
    void devePersistirCriadoEventoERetornarIssoEmListar() throws Exception {
        String payload = """
                {
                  "titulo": "Retiro Jovem",
                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000bb",
                  "inicio": "2026-07-10T14:00:00Z",
                  "fim": "2026-07-10T16:00:00Z"
                }
                """;

        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", "evt-persistencia-001")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000bb")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Retiro Jovem"));

        mockMvc.perform(get("/api/v1/eventos?size=100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].titulo").value(hasItem("Retiro Jovem")));

    }
}
