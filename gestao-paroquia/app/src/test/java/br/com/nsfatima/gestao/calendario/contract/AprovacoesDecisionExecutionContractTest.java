package br.com.nsfatima.gestao.calendario.contract;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class AprovacoesDecisionExecutionContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CONSELHO_ORG_ID = "00000000-0000-0000-0000-0000000000cc";

    @Test
    @SuppressWarnings("null")
    void approvedDecisionResponseContainsActionExecutionSchema() throws Exception {
        MvcResult pending = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "secretario")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", CONSELHO_ORG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                          "titulo": "Contrato decisao aprovada",
                          "organizacaoResponsavelId": "%s",
                          "inicio": "2027-07-10T10:00:00Z",
                          "fim": "2027-07-10T11:00:00Z"
                        }
                        """, CONSELHO_ORG_ID)))
                .andExpect(status().isAccepted())
                .andReturn();

        String approvalId = objectMapper.readTree(pending.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asText();

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", approvalId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "APROVADA"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(approvalId))
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.actionExecution.outcome").value("EXECUTED"))
                .andExpect(jsonPath("$.actionExecution.targetResourceId").isNotEmpty())
                .andExpect(jsonPath("$.actionExecution.targetStatus").isNotEmpty());
    }

    @Test
    @SuppressWarnings("null")
    void rejectedDecisionResponseContainsActionExecutionSchema() throws Exception {
        MvcResult pending = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "secretario")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", CONSELHO_ORG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                          "titulo": "Contrato decisao rejeitada",
                          "organizacaoResponsavelId": "%s",
                          "inicio": "2027-07-11T10:00:00Z",
                          "fim": "2027-07-11T11:00:00Z"
                        }
                        """, CONSELHO_ORG_ID)))
                .andExpect(status().isAccepted())
                .andReturn();

        String approvalId = objectMapper.readTree(pending.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asText();

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", approvalId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "REPROVADA"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(approvalId))
                .andExpect(jsonPath("$.status").value("REPROVADA"))
                .andExpect(jsonPath("$.actionExecution.outcome").value("REJECTED"))
                .andExpect(jsonPath("$.actionExecution.targetResourceId").isEmpty())
                .andExpect(jsonPath("$.actionExecution.errorCode").isEmpty());
    }
}
