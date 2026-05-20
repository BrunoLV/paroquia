package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
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
class RejectUpdateEventoAuditTrailIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void rejectionUpdatesAuditTrailWithDecidoEmUtcNull() throws Exception {
        // Create event
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento audit trail",
                          "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000bb",
                          "inicio": "2027-12-20T10:00:00Z",
                          "fim": "2027-12-20T11:00:00Z"
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        UUID eventoId = UUID.fromString(
                objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

        // PATCH with sensitive fields → 202 PENDING
        MvcResult patchResult = mockMvc.perform(patch("/api/v1/eventos/{id}", eventoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000bb")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "inicio": "2027-12-26T10:00:00Z"
                        }
                        """))
                .andExpect(status().isAccepted())
                .andReturn();

        UUID aprovacaoId = UUID.fromString(
                objectMapper.readTree(patchResult.getResponse().getContentAsString())
                        .get("solicitacaoAprovacaoId").asText());

        // Reject
        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "REPROVADA",
                          "observacao": "Conflita com reuniao"
                        }
                        """))
                .andExpect(status().isOk());

        // Verify audit state: REPROVADA + no executadoEmUtc + correct timestamps
        var aprovacao = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        assert aprovacao.getStatus().equals("REPROVADA");
        assert aprovacao.getExecutadoEmUtc() == null : "rejectedecution should have null executadoEmUtc";
        assert aprovacao.getDecididoEmUtc() != null : "rejected should set decidoEmUtc";
        assert aprovacao.getCriadoEmUtc() != null : "should have criadoEmUtc";
        assert aprovacao.getDecisionObservacao().equals("Conflita com reuniao") : "should capture observation";
    }
}
