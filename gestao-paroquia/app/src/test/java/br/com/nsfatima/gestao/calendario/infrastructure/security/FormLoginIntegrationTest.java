package br.com.nsfatima.gestao.calendario.infrastructure.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class FormLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveLoginComValidoCredenciaisERetornarSessaoCookie() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "joao.silva",
                                  "password": "senha123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("joao.silva"))
                .andReturn();

        Cookie sessionCookie = result.getResponse().getCookie("JSESSIONID");
        if (sessionCookie == null && result.getRequest().getSession(false) != null) {
            sessionCookie = new Cookie("JSESSIONID",
                    Objects.requireNonNull(result.getRequest().getSession(false)).getId());
        }
        assertNotNull(sessionCookie);
    }

    @Test
    void deveRejeitarInvalidoCredenciais() throws Exception {
        mockMvc.perform(post("/api/v1/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "joao.silva",
                                  "password": "senha-errada"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_INVALID"));
    }

    @Test
    void deveRejeitarDesabilitadoUsuarioComAprovacaoObrigatorio() throws Exception {
        mockMvc.perform(post("/api/v1/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "usuario.inativo",
                                  "password": "senha123"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("APROVACAO_OBRIGATORIA"))
                .andExpect(jsonPath("$.message").value("Cadastro pendente de aprovacao por um administrador."));
    }

    @Test
    void deveInvalidarSessaoEmLogout() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/v1/autenticacao/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "joao.silva",
                                  "password": "senha123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        Cookie sessionCookie = loginResult.getResponse().getCookie("JSESSIONID");
        if (sessionCookie == null && loginResult.getRequest().getSession(false) != null) {
            sessionCookie = new Cookie("JSESSIONID",
                    Objects.requireNonNull(loginResult.getRequest().getSession(false)).getId());
        }
        assertNotNull(sessionCookie);

        mockMvc.perform(post("/api/v1/autenticacao/logout").cookie(sessionCookie))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("JSESSIONID=")));

        mockMvc.perform(get("/api/v1/eventos").cookie(sessionCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("SESSION_EXPIRED"));
    }
}
