package br.com.nsfatima.gestao.organizacao.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrganizacaoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAndRetrieveOrganization() throws Exception {
        String payload = """
                {
                  "nome": "Pastoral da Música",
                  "tipo": "PASTORAL",
                  "contato": "musica@nsfatima.com",
                  "ativo": true
                }
                """;

        String response = mockMvc.perform(post("/api/v1/organizacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Pastoral da Música"))
                .andReturn().getResponse().getContentAsString();

        // Simple way to get ID from response
        String id = response.substring(response.indexOf("\"id\":\"") + 6, response.indexOf("\",\"nome\""));

        mockMvc.perform(get("/api/v1/organizacoes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pastoral da Música"))
                .andExpect(jsonPath("$.tipo").value("PASTORAL"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyCreateForNonAdmin() throws Exception {
        String payload = """
                {
                  "nome": "Tentativa",
                  "tipo": "PASTORAL"
                }
                """;

        mockMvc.perform(post("/api/v1/organizacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isForbidden());
    }
}
