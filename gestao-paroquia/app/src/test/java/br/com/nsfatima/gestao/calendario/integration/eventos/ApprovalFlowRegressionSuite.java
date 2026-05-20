package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ApprovalFlowRegressionSuite {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void shouldValidateCoreApprovalRegressionScenarios() throws Exception {
        long eventsBefore = eventoJpaRepository.count();

        MvcResult createPending = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "secretario")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000dd")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Suite create approve",
                          "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000dd",
                          "inicio": "2027-08-01T10:00:00Z",
                          "fim": "2027-08-01T11:00:00Z"
                        }
                        """))
                .andExpect(status().isAccepted())
                .andReturn();

        UUID createApprovalId = UUID.fromString(objectMapper.readTree(createPending.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asText());

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", createApprovalId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "APROVADA"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionExecution.outcome").value("EXECUTED"));

        MvcResult immediateCreate = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000dd")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Suite update reject",
                          "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000dd",
                          "inicio": "2027-08-10T10:00:00Z",
                          "fim": "2027-08-10T11:00:00Z"
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        UUID eventId = UUID.fromString(objectMapper.readTree(immediateCreate.getResponse().getContentAsString())
                .get("id").asText());

        MvcResult updatePending = mockMvc.perform(patch("/api/v1/eventos/{id}", eventId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000dd")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "inicio": "2027-08-15T10:00:00Z",
                          "fim": "2027-08-15T11:00:00Z"
                        }
                        """))
                .andExpect(status().isAccepted())
                .andReturn();

        UUID updateApprovalId = UUID.fromString(objectMapper.readTree(updatePending.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asText());

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", updateApprovalId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "REPROVADA"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionExecution.outcome").value("REJECTED"));

        var unchanged = eventoJpaRepository.findById(eventId).orElseThrow();
        assertThat(unchanged.getInicioUtc().toString()).contains("2027-08-10");

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", createApprovalId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "APROVADA"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("APPROVAL_ALREADY_DECIDED"));

        assertThat(eventoJpaRepository.count()).isGreaterThanOrEqualTo(eventsBefore + 2);
        assertThat(aprovacaoJpaRepository.findById(createApprovalId)).isPresent();
        assertThat(aprovacaoJpaRepository.findById(updateApprovalId)).isPresent();
    }
}
