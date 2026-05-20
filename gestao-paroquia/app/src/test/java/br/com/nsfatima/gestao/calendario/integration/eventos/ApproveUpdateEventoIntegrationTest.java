package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayload;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
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
class ApproveUpdateEventoIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private EventoJpaRepository eventoJpaRepository;

        @Autowired
        private AprovacaoJpaRepository aprovacaoJpaRepository;

        @Autowired
        private ApprovalActionPayloadMapper approvalActionPayloadMapper;

        @Test
        @SuppressWarnings("null")
        void shouldAutoUpdateEventoAfterApproval() throws Exception {
                // Step 1: Create event (paroco → immediate 201)
                MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", UUID.randomUUID())
                                .header("X-Actor-Role", "paroco")
                                .header("X-Actor-Org-Type", "CLERO")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "titulo": "Evento original",
                                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                                  "inicio": "2027-08-01T10:00:00Z",
                                                  "fim": "2027-08-01T11:00:00Z"
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andReturn();

                UUID eventoId = UUID.fromString(
                                objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id")
                                                .asText());

                // Step 2: PATCH with sensitive fields by coordenador/PASTORAL → 202 PENDING
                MvcResult patchResult = mockMvc.perform(patch("/api/v1/eventos/{id}", eventoId)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "PASTORAL")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "inicio": "2027-08-05T10:00:00Z",
                                                  "fim": "2027-08-05T11:00:00Z"
                                                }
                                                """))
                                .andExpect(status().isAccepted())
                                .andReturn();

                UUID aprovacaoId = UUID.fromString(
                                objectMapper.readTree(patchResult.getResponse().getContentAsString())
                                                .get("solicitacaoAprovacaoId").asText());

                // Event should not be changed yet
                var eventoBeforeApproval = eventoJpaRepository.findById(eventoId).orElseThrow();
                assertThat(eventoBeforeApproval.getInicioUtc().toString()).contains("2027-08-01");

                // Step 3: PATCH approve → update applied automatically
                mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "CONSELHO")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "status": "APROVADA",
                                                  "observacao": "Aprovado"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APROVADA"))
                                .andExpect(jsonPath("$.actionExecution.outcome").value("EXECUTED"))
                                .andExpect(jsonPath("$.actionExecution.eventoId").value(eventoId.toString()));

                // Event should now have updated fields
                var eventoAfterApproval = eventoJpaRepository.findById(eventoId).orElseThrow();
                assertThat(eventoAfterApproval.getInicioUtc().toString()).contains("2027-08-05");

                // Approval should have executadoEmUtc set
                var savedAprovacao = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
                assertThat(savedAprovacao.getExecutadoEmUtc()).isNotNull();
        }

        @Test
        @SuppressWarnings("null")
        void shouldApplyUpdateFromDirectPendingSnapshot() throws Exception {
                // Create event first
                MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", UUID.randomUUID())
                                .header("X-Actor-Role", "paroco")
                                .header("X-Actor-Org-Type", "CLERO")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "titulo": "Evento snapshot direto",
                                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                                  "inicio": "2027-09-01T09:00:00Z",
                                                  "fim": "2027-09-01T10:00:00Z"
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andReturn();

                UUID eventoId = UUID.fromString(
                                objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id")
                                                .asText());

                // Setup approval directly
                UUID aprovacaoId = UUID.randomUUID();
                ApprovalActionPayload payload = new ApprovalActionPayload(
                                null,
                                eventoId,
                                null,
                                null,
                                null,
                                null,
                                null,
                                Instant.parse("2027-09-10T09:00:00Z"),
                                Instant.parse("2027-09-10T10:00:00Z"),
                                null,
                                null,
                                null,
                                null,
                                null);

                AprovacaoEntity aprovacao = new AprovacaoEntity();
                aprovacao.setId(aprovacaoId);
                aprovacao.setEventoId(eventoId);
                aprovacao.setTipoSolicitacao("EDICAO_EVENTO");
                aprovacao.setStatus("PENDENTE");
                aprovacao.setCriadoEmUtc(Instant.now());
                aprovacao.setSolicitanteId(UUID.randomUUID().toString());
                aprovacao.setSolicitantePapel("coordenador");
                aprovacao.setSolicitanteTipoOrganizacao("PASTORAL");
                aprovacao.setCorrelationId(aprovacaoId.toString());
                aprovacao.setAprovadorPapel("conselho-coordenador");
                aprovacao.setActionPayloadJson(approvalActionPayloadMapper.toJson(payload));
                aprovacaoJpaRepository.save(aprovacao);

                mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "CONSELHO")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "status": "APROVADA",
                                                  "observacao": "Aprovado snapshot"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.actionExecution.outcome").value("EXECUTED"))
                                .andExpect(jsonPath("$.actionExecution.eventoId").value(eventoId.toString()));

                var eventoAfter = eventoJpaRepository.findById(eventoId).orElseThrow();
                assertThat(eventoAfter.getInicioUtc().toString()).contains("2027-09-10");
        }
}
