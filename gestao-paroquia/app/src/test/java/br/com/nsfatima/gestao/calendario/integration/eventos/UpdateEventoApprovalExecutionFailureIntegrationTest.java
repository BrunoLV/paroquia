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
class UpdateEventoApprovalExecutionFailureIntegrationTest {

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
        void approvedUpdateWithInvalidPayloadResultsIn500WithoutEventChange() throws Exception {
                // Create event first
                MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", UUID.randomUUID())
                                .header("X-Actor-Role", "paroco")
                                .header("X-Actor-Org-Type", "CLERO")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "titulo": "Evento falha execucao",
                                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                                  "inicio": "2027-11-01T10:00:00Z",
                                                  "fim": "2027-11-01T11:00:00Z"
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andReturn();

                UUID eventoId = UUID.fromString(
                                objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id")
                                                .asText());

                // Setup approval with invalid/non-existent eventoId in payload
                UUID aprovacaoId = UUID.randomUUID();
                UUID nonExistentEventoId = UUID.randomUUID();

                ApprovalActionPayload payload = new ApprovalActionPayload(
                                null,
                                nonExistentEventoId,
                                null,
                                null,
                                null,
                                null,
                                null,
                                Instant.parse("2027-11-15T10:00:00Z"),
                                Instant.parse("2027-11-15T11:00:00Z"),
                                null,
                                null,
                                null,
                                null,
                                null);

                AprovacaoEntity aprovacao = new AprovacaoEntity();
                aprovacao.setId(aprovacaoId);
                aprovacao.setEventoId(nonExistentEventoId);
                aprovacao.setTipoSolicitacao("EDICAO_EVENTO");
                aprovacao.setAprovadorPapel("conselho-coordenador");
                aprovacao.setStatus("PENDENTE");
                aprovacao.setCriadoEmUtc(Instant.now());
                aprovacao.setSolicitanteId(UUID.randomUUID().toString());
                aprovacao.setSolicitantePapel("coordenador");
                aprovacao.setSolicitanteTipoOrganizacao("PASTORAL");
                aprovacao.setCorrelationId(aprovacaoId.toString());
                aprovacao.setActionPayloadJson(approvalActionPayloadMapper.toJson(payload));
                aprovacaoJpaRepository.save(aprovacao);

                long eventoCountBefore = eventoJpaRepository.count();

                // Approve -> execution fails (evento not found) -> 409 with deterministic code
                mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "CONSELHO")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "status": "APROVADA",
                                                  "observacao": "Tentativa de execucao"
                                                }
                                                """))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.errorCode").value("APPROVAL_EXECUTION_FAILED"));

                // Original event unchanged
                assertThat(eventoJpaRepository.count()).isEqualTo(eventoCountBefore);
                var evento = eventoJpaRepository.findById(eventoId).orElseThrow();
                assertThat(evento.getInicioUtc().toString()).contains("2027-11-01");

                // Approval persisted as FALHA_EXECUCAO
                var savedAprovacao = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
                assertThat(savedAprovacao.getStatus()).isEqualTo("FALHA_EXECUCAO");
                assertThat(savedAprovacao.getExecutadoEmUtc()).isNull();
        }
}
