package br.com.nsfatima.gestao.calendario.infrastructure.security;

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
class RbacOrganizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowCreateForConselhoSecretary() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "maria.secretaria", "senha123");

        mockMvc.perform(post("/api/v1/eventos")
                .session(session)
                .header("Idempotency-Key", "rbac-create-secretaria")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreatePayload("00000000-0000-0000-0000-0000000000cc")))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void shouldReturnRoleScopeInvalidForIncompatibleRoleAndOrganizationType() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "clara.invalida", "senha123");

        mockMvc.perform(post("/api/v1/eventos")
                .session(session)
                .header("Idempotency-Key", "rbac-role-scope-invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreatePayload("00000000-0000-0000-0000-0000000000dd")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ROLE_SCOPE_INVALID"));
    }

    @Test
    void shouldDenyCreateForMemberWithoutWriteRole() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "pedro.membro", "senha123");

        mockMvc.perform(post("/api/v1/eventos")
                .session(session)
                .header("Idempotency-Key", "rbac-member-denied")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCreatePayload("00000000-0000-0000-0000-0000000000aa")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void shouldAllowAuthenticatedReadForPastoralCoordinator() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "joao.silva", "senha123");

        mockMvc.perform(get("/api/v1/eventos")
                        .session(session)
                        .header("X-Actor-Org-Id", TestAuditOrganizationResolver.resolveOrgId("joao.silva")))
                .andExpect(status().isOk());
    }

    private String validCreatePayload(String organizacaoResponsavelId) {
        return """
                {
                  "titulo": "Evento RBAC",
                  "descricao": "Teste de seguranca",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-08-20T10:00:00Z",
                  "fim": "2026-08-20T11:00:00Z",
                  "status": "rascunho",
                  "participantes": []
                }
                """.formatted(organizacaoResponsavelId);
    }
}
