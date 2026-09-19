package br.com.nsfatima.gestao.calendario.api.error;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@Import(ExceptionHandlerIntegrationTest.TestErrorController.class)
class ExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornarBadRequestEmTipoMismatch() throws Exception {
        mockMvc.perform(get("/api/v1/eventos/not-a-uuid")
                        .header("X-Actor-Role", "admin")
                        .header("X-Actor-Org-Type", "CONSELHO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FIELD_INVALID"))
                .andExpect(jsonPath("$.message").value("Tipo de dado inválido para o parâmetro: eventoId"))
                .andExpect(jsonPath("$.errors[0].field").value("eventoId"));
    }

    @Test
    void deveRetornarProibidoEmProibidoOperacao() throws Exception {
        mockMvc.perform(get("/api/v1/test-errors/forbidden"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Operação não permitida"))
                .andExpect(jsonPath("$.errors[0].field").value("authorization"))
                .andExpect(jsonPath("$.errors[0].message").value("Operação não permitida"));
    }

    @Test
    void deveRetornarProibidoEmAcessoNegado() throws Exception {
        mockMvc.perform(get("/api/v1/test-errors/access-denied"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message").value("Acesso negado"))
                .andExpect(jsonPath("$.errors[0].field").value("authorization"))
                .andExpect(jsonPath("$.errors[0].message").value("Acesso negado"));
    }

    @Test
    void deveRetornarConflitoEmDataIntegrityViolacao() throws Exception {
        mockMvc.perform(get("/api/v1/test-errors/integrity-violation"))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Conflito: já existe um registro cadastrado com estes dados."))
                .andExpect(jsonPath("$.errors[0].field").value("database"));
    }

    @RestController
    @RequestMapping("/api/v1/test-errors")
    static class TestErrorController {
        @GetMapping("/forbidden")
        void throwForbidden() {
            throw new OperacaoProibidaException("Operação não permitida");
        }

        @GetMapping("/access-denied")
        void throwAccessDenied() {
            throw new AccessDeniedException("Acesso negado");
        }

        @GetMapping("/integrity-violation")
        void throwIntegrityViolation() {
            throw new DataIntegrityViolationException(
                    "database violation",
                    new SQLIntegrityConstraintViolationException("duplicate key value violates unique constraint")
            );
        }
    }
}


