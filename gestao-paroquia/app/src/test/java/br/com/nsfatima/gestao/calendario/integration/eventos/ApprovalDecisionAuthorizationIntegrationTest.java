package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class ApprovalDecisionAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @SuppressWarnings("null")
    void secretarioTryingToApproveReturns403() throws Exception {
        // Create event
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Evento auth test",
                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                  "inicio": "2027-12-20T10:00:00Z",
                                  "fim": "2027-12-20T11:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        UUID eventoId = UUID.fromString(
                objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asString());

        // PATCH with sensitive fields by coordenador/PASTORAL → 202 PENDING
        MvcResult patchResult = mockMvc.perform(patch("/api/v1/eventos/{id}", eventoId)
                        .header("X-Actor-Role", "coordenador-pastoral-local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inicio": "2027-12-25T10:00:00Z"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        String aprovacaoIdStr = objectMapper.readTree(patchResult.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asString();

        // secretario/CONSELHO trying to approve → 403 (not allowed to decide, only
        // coordenador/paroco can)
        assertForbiddenApprovalDecision(aprovacaoIdStr, "secretario", "CONSELHO", """
                {
                  "status": "APROVADA",
                  "observacao": "Unauthorized attempt"
                }
                """);
    }

    @Test
    @SuppressWarnings("null")
    void mbroTryingToApproveReturns403() throws Exception {
        // Create event
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Evento auth test 2",
                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000bb",
                                  "inicio": "2027-12-20T10:00:00Z",
                                  "fim": "2027-12-20T11:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        UUID eventoId = UUID.fromString(
                objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asString());

        // PATCH with sensitive fields by coordenador/PASTORAL → 202 PENDING
        MvcResult patchResult = mockMvc.perform(patch("/api/v1/eventos/{id}", eventoId)
                        .header("X-Actor-Role", "coordenador-pastoral-local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000bb")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fim": "2027-12-25T11:00:00Z"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        String aprovacaoIdStr = objectMapper.readTree(patchResult.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asString();

        // membro/PASTORAL trying to approve → 403 (no authority)
        assertForbiddenApprovalDecision(aprovacaoIdStr, "membro", "PASTORAL", """
                {
                        "status": "APROVADA"
                }
                """);
    }

    @SuppressWarnings("null")
    private void assertForbiddenApprovalDecision(String aprovacaoIdStr, String role, String organizationType,
                                                 String content) throws Exception {
        try {
            mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoIdStr)
                            .header("X-Actor-Role", role)
                            .header("X-Actor-Org-Type", organizationType)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(content))
                    .andExpect(status().isForbidden());
        } catch (ServletException ex) {
            assertInstanceOf(ForbiddenOperationException.class, ex.getCause());
        }
    }
}
