package br.com.nsfatima.gestao.calendario.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
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
class EventosPatchPendingContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    private static final String PASTORAL_A = "00000000-0000-0000-0000-0000000000aa";
    private static final String CLERO_D = "00000000-0000-0000-0000-0000000000dd";

    @Test
    @SuppressWarnings("null")
    void patchWithSensitiveFieldsByCoordenadorRequiringApprovalReturns202() throws Exception {
        // Create event first
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .header("X-Actor-Org-Id", CLERO_D)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {
                          "titulo": "Evento para contrato",
                          "organizacaoResponsavelId": "%s",
                          "inicio": "2027-12-01T10:00:00Z",
                          "fim": "2027-12-01T11:00:00Z"
                        }
                        """, PASTORAL_A)))
                .andExpect(status().isCreated())
                .andReturn();

        String eventoIdStr = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        // Contract: PATCH with sensitive field + coordenador/PASTORAL → 202 ACCEPTED
        mockMvc.perform(patch("/api/v1/eventos/{id}", eventoIdStr)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", PASTORAL_A)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "inicio": "2027-12-15T10:00:00Z"
                        }
                        """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.solicitacaoAprovacaoId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.mensagem").value("APPROVAL_PENDING"));
    }
}
