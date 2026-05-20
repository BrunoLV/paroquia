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
class ApprovalAutoExecutionFailureConsistencyIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private ApprovalActionPayloadMapper approvalActionPayloadMapper;

        @Autowired
        private AprovacaoJpaRepository aprovacaoJpaRepository;

        @Autowired
        private EventoJpaRepository eventoJpaRepository;

        @Test
        @SuppressWarnings("null")
        void createFailureKeepsStateConsistentAndReturnsDeterministicError() throws Exception {
                long countBefore = eventoJpaRepository.count();

                UUID approvalId = UUID.randomUUID();
                ApprovalActionPayload invalidPayload = new ApprovalActionPayload(
                                UUID.randomUUID().toString(),
                                null,
                                "Evento invalido sem organizacao",
                                null,
                                null,
                                null,
                                null,
                                Instant.parse("2027-05-10T10:00:00Z"),
                                Instant.parse("2027-05-10T11:00:00Z"),
                                null,
                                null,
                                null,
                                null,
                                null);

                AprovacaoEntity approval = new AprovacaoEntity();
                approval.setId(approvalId);
                approval.setTipoSolicitacao("CRIACAO_EVENTO");
                approval.setAprovadorPapel("conselho-coordenador");
                approval.setStatus("PENDENTE");
                approval.setCriadoEmUtc(Instant.now());
                approval.setSolicitanteId(UUID.randomUUID().toString());
                approval.setSolicitantePapel("secretario");
                approval.setSolicitanteTipoOrganizacao("CONSELHO");
                approval.setCorrelationId(approvalId.toString());
                approval.setActionPayloadJson(approvalActionPayloadMapper.toJson(invalidPayload));
                aprovacaoJpaRepository.save(approval);

                mockMvc.perform(patch("/api/v1/aprovacoes/{id}", approvalId)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "CONSELHO")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "status": "APROVADA"
                                                }
                                                """))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.errorCode").value("APPROVAL_EXECUTION_FAILED"));

                assertThat(eventoJpaRepository.count()).isEqualTo(countBefore);
                var saved = aprovacaoJpaRepository.findById(approvalId).orElseThrow();
                assertThat(saved.getStatus()).isEqualTo("FALHA_EXECUCAO");
                assertThat(saved.getMensagemErroExecucao()).isNotNull();
                assertThat(saved.getExecutadoEmUtc()).isNull();
        }

        @Test
        @SuppressWarnings("null")
        void updateFailureKeepsStateConsistentAndReturnsDeterministicError() throws Exception {
                MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                                .header("Idempotency-Key", UUID.randomUUID())
                                .header("X-Actor-Role", "paroco")
                                .header("X-Actor-Org-Type", "CLERO")
                                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000cc")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "titulo": "Evento base para falha update",
                                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000cc",
                                                  "inicio": "2027-06-01T10:00:00Z",
                                                  "fim": "2027-06-01T11:00:00Z"
                                                }
                                                """))
                                .andExpect(status().isCreated())
                                .andReturn();

                UUID realEventId = UUID
                                .fromString(objectMapper.readTree(createResult.getResponse().getContentAsString())
                                                .get("id").asText());
                long countBefore = eventoJpaRepository.count();

                UUID approvalId = UUID.randomUUID();
                UUID nonexistentEventId = UUID.randomUUID();

                ApprovalActionPayload invalidPayload = new ApprovalActionPayload(
                                null,
                                nonexistentEventId,
                                null,
                                null,
                                null,
                                null,
                                null,
                                Instant.parse("2027-06-15T10:00:00Z"),
                                Instant.parse("2027-06-15T11:00:00Z"),
                                null,
                                null,
                                null,
                                null,
                                null);

                AprovacaoEntity approval = new AprovacaoEntity();
                approval.setId(approvalId);
                approval.setEventoId(nonexistentEventId);
                approval.setTipoSolicitacao("EDICAO_EVENTO");
                approval.setAprovadorPapel("conselho-coordenador");
                approval.setStatus("PENDENTE");
                approval.setCriadoEmUtc(Instant.now());
                approval.setSolicitanteId(UUID.randomUUID().toString());
                approval.setSolicitantePapel("coordenador");
                approval.setSolicitanteTipoOrganizacao("PASTORAL");
                approval.setCorrelationId(approvalId.toString());
                approval.setActionPayloadJson(approvalActionPayloadMapper.toJson(invalidPayload));
                aprovacaoJpaRepository.save(approval);

                mockMvc.perform(patch("/api/v1/aprovacoes/{id}", approvalId)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "CONSELHO")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "status": "APROVADA"
                                                }
                                                """))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.errorCode").value("APPROVAL_EXECUTION_FAILED"));

                assertThat(eventoJpaRepository.count()).isEqualTo(countBefore);
                assertThat(eventoJpaRepository.findById(realEventId)).isPresent();

                var saved = aprovacaoJpaRepository.findById(approvalId).orElseThrow();
                assertThat(saved.getStatus()).isEqualTo("FALHA_EXECUCAO");
                assertThat(saved.getMensagemErroExecucao()).isNotNull();
                assertThat(saved.getExecutadoEmUtc()).isNull();
        }
}
