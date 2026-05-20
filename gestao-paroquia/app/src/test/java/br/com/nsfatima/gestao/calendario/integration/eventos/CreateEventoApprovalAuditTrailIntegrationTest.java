package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class CreateEventoApprovalAuditTrailIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void shouldPersistApprovalTimelineForCreateExecution() throws Exception {
        long eventCountBefore = eventoJpaRepository.count();

        MvcResult pendingResult = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "secretario")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000cc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento trilha create",
                          "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000cc",
                          "inicio": "2027-03-20T10:00:00Z",
                          "fim": "2027-03-20T11:00:00Z"
                        }
                        """))
                .andExpect(status().isAccepted())
                .andReturn();

        UUID approvalId = UUID.fromString(objectMapper.readTree(pendingResult.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asText());

        var pending = aprovacaoJpaRepository.findById(approvalId).orElseThrow();
        assertThat(pending.getStatus()).isEqualTo("PENDENTE");
        assertThat(pending.getCriadoEmUtc()).isNotNull();
        assertThat(pending.getDecididoEmUtc()).isNull();
        assertThat(pending.getExecutadoEmUtc()).isNull();
        assertThat(pending.getActionPayloadJson()).contains("Evento trilha create");

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", approvalId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "APROVADA",
                          "observacao": "ok"
                        }
                        """))
                .andExpect(status().isOk());

        var decided = aprovacaoJpaRepository.findById(approvalId).orElseThrow();
        assertThat(decided.getStatus()).isEqualTo("APROVADA");
        assertThat(decided.getDecididoEmUtc()).isNotNull();
        assertThat(decided.getExecutadoEmUtc()).isNotNull();
        assertThat(decided.getEventoId()).isNotNull();
        assertThat(eventoJpaRepository.count()).isEqualTo(eventCountBefore + 1);
    }
}
