package br.com.nsfatima.gestao.iam.api.v1.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/sql/security-fixtures.sql")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithUserDetails("joao.silva")
    void deveRetornarUsuarioProfileComOrganizacoes() throws Exception {
        mockMvc.perform(get("/api/v1/autenticacao/meu-perfil"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("joao.silva")))
                .andExpect(jsonPath("$.organizations", notNullValue()))
                .andExpect(jsonPath("$.organizations", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.organizations[0].nome", is("Pastoral A")));
    }

    @Test
    @WithUserDetails("joao.silva")
    void deveAlterarPropriaSenhaComSucesso() throws Exception {
        String payload = """
                {
                    "senhaAtual": "senha123",
                    "novaSenha": "novaSenha@2026"
                }
                """;
        mockMvc.perform(post("/api/v1/autenticacao/alterar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails("joao.silva")
    void deveRejeitarAlteracaoQuandoSenhaAtualIncorreta() throws Exception {
        String payload = """
                {
                    "senhaAtual": "senhaInvalida",
                    "novaSenha": "novaSenha@2026"
                }
                """;
        mockMvc.perform(post("/api/v1/autenticacao/alterar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAceitarSolicitacaoPublicaDeResetDeSenha() throws Exception {
        String payload = """
                {
                    "username": "joao.silva"
                }
                """;
        mockMvc.perform(post("/api/v1/autenticacao/solicitar-reset-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isNoContent());
    }
}
