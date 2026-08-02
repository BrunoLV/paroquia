package br.com.nsfatima.gestao.calendario.infrastructure.security;

import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class FailClosedAuthzIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeitorVinculoExterno externalVinculoReader;

    @Test
    void deveRetornarServiceUnavailableQuandoAuthorizationSourceFalha() throws Exception {
        when(externalVinculoReader.findVinculosAtivos(any(UUID.class)))
                .thenThrow(new CannotAcquireLockException("db unavailable"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "joao.silva",
                                  "password": "senha123"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.errorCode").value("AUTHZ_SOURCE_UNAVAILABLE"));
    }
}
