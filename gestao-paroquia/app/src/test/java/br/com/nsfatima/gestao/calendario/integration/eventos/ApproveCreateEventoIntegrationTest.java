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
class ApproveCreateEventoIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private EventoJpaRepository eventoJpaRepository;

        @Autowired
        private AprovacaoJpaRepository aprovacaoJpaRepository;

        @Autowired
        private ApprovalActionPayloadMapper approvalActionPayloadMapper;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @SuppressWarnings("null")
        void shouldAutoCreateEventoAfterApproval() throws Exception {
                // Step 1: POST with secretario → 202 PENDING
                UUID idempotencyKey = UUID.randomUUID();
                long eventsBefore = eventoJpaRepository.count();

                MvcResult postResult = mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", idempotencyKey)
                                .header("X-Actor-Role", "secretario")
                                .header("X-Actor-Org-Type", "CONSELHO")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000cc")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "titulo": "Evento auto-criado pos-aprovacao",
                                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000cc",
                                                  "inicio": "2027-04-01T10:00:00Z",
                                                  "fim": "2027-04-01T11:00:00Z"
                                                }
                                                """))
                                .andExpect(status().isAccepted())
                                .andExpect(jsonPath("$.solicitacaoAprovacaoId").isNotEmpty())
                                .andReturn();

                String postBody = postResult.getResponse().getContentAsString();
                String aprovacaoIdStr = objectMapper.readTree(postBody).get("solicitacaoAprovacaoId").asText();
                UUID aprovacaoId = UUID.fromString(aprovacaoIdStr);

                assertThat(eventoJpaRepository.count()).isEqualTo(eventsBefore);

                // Step 2: PATCH approve → event created automatically
                mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "CONSELHO")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "status": "APROVADA",
                                                  "observacao": "Aprovado pelo coordenador"
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("APROVADA"))
                                .andExpect(jsonPath("$.actionExecution.outcome").value("EXECUTED"))
                                .andExpect(jsonPath("$.actionExecution.eventoId").isNotEmpty());

                // Verify event was created
                assertThat(eventoJpaRepository.count()).isEqualTo(eventsBefore + 1);

                // Verify approval updated with eventoId
                AprovacaoEntity savedAprovacao = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
                assertThat(savedAprovacao.getStatus()).isEqualTo("APROVADA");
                assertThat(savedAprovacao.getEventoId()).isNotNull();
                var evento = eventoJpaRepository.findById(savedAprovacao.getEventoId()).orElseThrow();
                assertThat(evento.getTitulo()).isEqualTo("Evento auto-criado pos-aprovacao");
                assertThat(savedAprovacao.getExecutadoEmUtc()).isNotNull();
        }

        @Test
        @SuppressWarnings("null")
        void shouldAutoCreateEventoFromDirectPendingAprovacao() throws Exception {
                // Setup a pending CRIACAO_EVENTO approval with proper payload
                UUID aprovacaoId = UUID.randomUUID();
                UUID orgId = UUID.fromString("00000000-0000-0000-0000-0000000000dd");

                ApprovalActionPayload payload = new ApprovalActionPayload(
                                UUID.randomUUID().toString(),
                                null,
                                "Evento direto do snapshot",
                                null,
                                null,
                                orgId,
                                null,
                                Instant.parse("2027-05-10T09:00:00Z"),
                                Instant.parse("2027-05-10T10:00:00Z"),
                                null,
                                null,
                                null,
                                null,
                                null);

                AprovacaoEntity aprovacao = new AprovacaoEntity();
                aprovacao.setId(aprovacaoId);
                aprovacao.setEventoId(null);
                aprovacao.setTipoSolicitacao("CRIACAO_EVENTO");
                aprovacao.setAprovadorPapel("conselho-coordenador");
                aprovacao.setStatus("PENDENTE");
                aprovacao.setCriadoEmUtc(Instant.now());
                aprovacao.setActionPayloadJson(approvalActionPayloadMapper.toJson(payload));
                aprovacaoJpaRepository.save(aprovacao);

                long eventsBefore = eventoJpaRepository.count();

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
                                .andExpect(jsonPath("$.actionExecution.outcome").value("EXECUTED"))
                                .andExpect(jsonPath("$.actionExecution.eventoId").isNotEmpty());

                assertThat(eventoJpaRepository.count()).isEqualTo(eventsBefore + 1);

                AprovacaoEntity saved = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
                assertThat(saved.getEventoId()).isNotNull();
        }
}
