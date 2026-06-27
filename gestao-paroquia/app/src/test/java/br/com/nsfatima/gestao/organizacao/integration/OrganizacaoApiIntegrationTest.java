package br.com.nsfatima.gestao.organizacao.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
                  "coordenador": "Pedro Coordenador",
                  "viceCoordenador": "Ana Vice",
                  "ativo": true
                }
                """;

        String response = mockMvc.perform(post("/api/v1/organizacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Pastoral da Música"))
                .andExpect(jsonPath("$.coordenador").value("Pedro Coordenador"))
                .andExpect(jsonPath("$.viceCoordenador").value("Ana Vice"))
                .andReturn().getResponse().getContentAsString();

        // Simple way to get ID from response
        String id = response.substring(response.indexOf("\"id\":\"") + 6, response.indexOf("\",\"nome\""));

        mockMvc.perform(get("/api/v1/organizacoes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pastoral da Música"))
                .andExpect(jsonPath("$.tipo").value("PASTORAL"))
                .andExpect(jsonPath("$.coordenador").value("Pedro Coordenador"))
                .andExpect(jsonPath("$.viceCoordenador").value("Ana Vice"));
    }

    @Test
    @WithMockUser(roles = "CONSELHO_COORDENADOR")
    void shouldAllowCreateForConselhoCoordenador() throws Exception {
        String payload = """
                {
                  "nome": "Pastoral da Acolhida",
                  "tipo": "PASTORAL",
                  "contato": "acolhida@nsfatima.com",
                  "coordenador": "Carlos Coordenador",
                  "viceCoordenador": "Lucas Vice",
                  "ativo": true
                }
                """;

        mockMvc.perform(post("/api/v1/organizacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Pastoral da Acolhida"))
                .andExpect(jsonPath("$.coordenador").value("Carlos Coordenador"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyCreateForNonAdmin() throws Exception {
        String payload = """
                {
                  "nome": "Tentativa",
                  "tipo": "PASTORAL",
                  "ativo": true
                }
                """;

        mockMvc.perform(post("/api/v1/organizacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldListOrganizationMembers() throws Exception {
        // Create a mock UUID
        java.util.UUID orgId = java.util.UUID.randomUUID();
        mockMvc.perform(get("/api/v1/organizacoes/" + orgId + "/membros")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
