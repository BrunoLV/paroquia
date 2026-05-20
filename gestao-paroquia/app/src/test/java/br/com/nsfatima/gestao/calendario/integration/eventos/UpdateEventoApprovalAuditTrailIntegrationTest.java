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
class UpdateEventoApprovalAuditTrailIntegrationTest {

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
    void shouldPersistApprovalTimelineForUpdateExecution() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000cc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento trilha update",
                          "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000cc",
                          "inicio": "2027-04-20T10:00:00Z",
                          "fim": "2027-04-20T11:00:00Z"
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        UUID eventoId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText());

        MvcResult pendingResult = mockMvc.perform(patch("/api/v1/eventos/{id}", eventoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000cc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                                                                                                "inicio": "2027-04-22T10:00:00Z",
                                                                                                                "fim": "2027-04-22T11:00:00Z"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        UUID approvalId = UUID.fromString(objectMapper.readTree(pendingResult.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asText());

        var pending = aprovacaoJpaRepository.findById(approvalId).orElseThrow();
        assertThat(pending.getStatus()).isEqualTo("PENDENTE");
        assertThat(pending.getEventoId()).isEqualTo(eventoId);
        assertThat(pending.getCriadoEmUtc()).isNotNull();
        assertThat(pending.getDecididoEmUtc()).isNull();

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", approvalId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "APROVADA"
                        }
                        """))
                .andExpect(status().isOk());

        var decided = aprovacaoJpaRepository.findById(approvalId).orElseThrow();
        assertThat(decided.getStatus()).isEqualTo("APROVADA");
        assertThat(decided.getDecididoEmUtc()).isNotNull();
        assertThat(decided.getExecutadoEmUtc()).isNotNull();

        var evento = eventoJpaRepository.findById(eventoId).orElseThrow();
        assertThat(evento.getInicioUtc().toString()).contains("2027-04-22");
    }
}
