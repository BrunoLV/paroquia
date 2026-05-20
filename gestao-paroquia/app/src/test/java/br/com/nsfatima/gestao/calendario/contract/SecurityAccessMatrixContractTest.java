package br.com.nsfatima.gestao.calendario.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import br.com.nsfatima.gestao.calendario.support.SecurityTestSupport;
import br.com.nsfatima.gestao.calendario.support.TestAuditOrganizationResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class SecurityAccessMatrixContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldKeepOnlyLoginPublicAcrossRepresentativeEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/eventos").header("X-Test-Anonymous", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_REQUIRED"));

        mockMvc.perform(post("/api/v1/aprovacoes")
                        .header("X-Test-Anonymous", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventoId\":\"00000000-0000-0000-0000-000000000001\",\"tipoSolicitacao\":\"alteracao_horario\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("AUTH_REQUIRED"));
    }

    @Test
    void shouldAllowAuthenticatedSessionOnProtectedReadEndpoints() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "joao.silva", "senha123");

        mockMvc.perform(get("/api/v1/eventos")
                        .session(session)
                        .header("X-Actor-Org-Id", TestAuditOrganizationResolver.resolveOrgId("joao.silva")))
                .andExpect(status().isOk());
    }
}
