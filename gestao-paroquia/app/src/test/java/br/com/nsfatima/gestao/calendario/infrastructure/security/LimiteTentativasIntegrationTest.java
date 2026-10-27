package br.com.nsfatima.gestao.calendario.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cada teste usa um username único porque os limitadores são singletons do contexto compartilhado
 * e o MockMvc origina todas as requisições do mesmo IP.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class LimiteTentativasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Login deve responder 429 com Retry-After após exceder falhas, inclusive com senha correta")
    void loginDeveBloquearAposFalhasRepetidas() throws Exception {
        String username = "forca.bruta." + UUID.randomUUID();

        for (int i = 0; i < 5; i++) {
            tentarLogin(username, "senha-errada").andExpect(status().isUnauthorized());
        }

        tentarLogin(username, "senha-errada")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.errorCode").value("TOO_MANY_REQUESTS"));
    }

    @Test
    @DisplayName("Login bem-sucedido não deve acumular tentativas")
    void loginComSucessoNaoDeveSerLimitado() throws Exception {
        for (int i = 0; i < 8; i++) {
            tentarLogin("joao.silva", "senha123").andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Solicitação de reset de senha deve responder 429 após exceder o limite de pedidos")
    void resetSenhaDeveSerLimitado() throws Exception {
        String username = "reset." + UUID.randomUUID();

        for (int i = 0; i < 3; i++) {
            pedirReset(username).andExpect(status().isNoContent());
        }

        pedirReset(username)
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    private ResultActions tentarLogin(String username, String senha)
            throws Exception {
        return mockMvc.perform(post("/api/v1/autenticacao/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, senha)));
    }

    private ResultActions pedirReset(String username) throws Exception {
        return mockMvc.perform(post("/api/v1/autenticacao/solicitar-reset-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\"}".formatted(username)));
    }
}
