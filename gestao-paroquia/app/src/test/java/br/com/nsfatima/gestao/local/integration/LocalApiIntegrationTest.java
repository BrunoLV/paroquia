package br.com.nsfatima.gestao.local.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LocalApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAndRetrieveLocal() throws Exception {
        String payload = """
                {
                  "nome": "Sala de Reuniões",
                  "tipo": "SALA",
                  "endereco": "2º Andar",
                  "capacidade": 20,
                  "caracteristicas": "Mesa redonda, 20 cadeiras",
                  "ativo": true
                }
                """;

        String response = mockMvc.perform(post("/api/v1/locais")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Sala de Reuniões"))
                .andReturn().getResponse().getContentAsString();

        // Simple way to get ID from response
        String id = response.substring(response.indexOf("\"id\":\"") + 6, response.indexOf("\",\"nome\""));

        mockMvc.perform(get("/api/v1/locais/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Sala de Reuniões"))
                .andExpect(jsonPath("$.capacidade").value(20));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyCreateForNonAdmin() throws Exception {
        String payload = """
                {
                  "nome": "Tentativa",
                  "tipo": "SALA"
                }
                """;

        mockMvc.perform(post("/api/v1/locais")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListLocais() throws Exception {
        mockMvc.perform(get("/api/v1/locais"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
