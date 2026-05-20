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
class UpdateEventoApprovalSnapshotImmutabilityIntegrationTest {

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
    void snapshotRemainsUnchangedAfterApprovalExecution() throws Exception {
        // Create event
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento snapshot",
                          "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000bb",
                          "inicio": "2027-12-20T10:00:00Z",
                          "fim": "2027-12-20T11:00:00Z"
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        UUID eventoId = UUID.fromString(
                objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

        // PATCH with sensitive fields
        MvcResult patchResult = mockMvc.perform(patch("/api/v1/eventos/{id}", eventoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000bb")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                                                                                                "inicio": "2027-12-25T10:00:00Z",
                                                                                                                "fim": "2027-12-25T11:00:00Z"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        UUID aprovacaoId = UUID.fromString(
                objectMapper.readTree(patchResult.getResponse().getContentAsString())
                        .get("solicitacaoAprovacaoId").asText());

        var aprovacaoBefore = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        String snapshotBefore = aprovacaoBefore.getActionPayloadJson();

        // Approve
        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "APROVADA"
                        }
                        """))
                .andExpect(status().isOk());

        // Snapshot should not change after execution
        var aprovacaoAfter = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        assertThat(aprovacaoAfter.getActionPayloadJson()).isEqualTo(snapshotBefore);
    }

    @Test
    @SuppressWarnings("null")
    void snapshotRemainsUnchangedAfterRejection() throws Exception {
        // Create event
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento snapshot rejeicao",
                          "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000bb",
                          "inicio": "2027-12-20T10:00:00Z",
                          "fim": "2027-12-20T11:00:00Z"
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        UUID eventoId = UUID.fromString(
                objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText());

        // PATCH with sensitive fields
        MvcResult patchResult = mockMvc.perform(patch("/api/v1/eventos/{id}", eventoId)
                .header("X-Actor-Role", "coordenador")
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

        UUID aprovacaoId = UUID.fromString(
                objectMapper.readTree(patchResult.getResponse().getContentAsString())
                        .get("solicitacaoAprovacaoId").asText());

        var aprovacaoBefore = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        String snapshotBefore = aprovacaoBefore.getActionPayloadJson();

        // Reject
        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "REPROVADA"
                        }
                        """))
                .andExpect(status().isOk());

        // Snapshot should not change after rejection
        var aprovacaoAfter = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        assertThat(aprovacaoAfter.getActionPayloadJson()).isEqualTo(snapshotBefore);
    }
}
