package br.com.nsfatima.gestao.calendario.infrastructure.security;

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
class SecurityEndpointAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRequireAuthenticationForBusinessEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/eventos").header("X-Test-Anonymous", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_REQUIRED"));

        mockMvc.perform(post("/api/v1/eventos")
                        .header("X-Test-Anonymous", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_REQUIRED"));

        mockMvc.perform(post("/api/v1/aprovacoes")
                        .header("X-Test-Anonymous", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventoId\":\"00000000-0000-0000-0000-000000000001\",\"tipoSolicitacao\":\"alteracao_horario\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_REQUIRED"));
    }
}
