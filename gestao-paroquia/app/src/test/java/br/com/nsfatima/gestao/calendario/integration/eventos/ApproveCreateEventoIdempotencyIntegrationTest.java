package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayload;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApproveCreateEventoIdempotencyIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private EventoJpaRepository eventoJpaRepository;

        @Autowired
        private AprovacaoJpaRepository aprovacaoJpaRepository;

        @Autowired
        private ApprovalActionPayloadMapper approvalActionPayloadMapper;

        @Test
        @SuppressWarnings("null")
        void approvedCreateUsesIdempotencyKeyFromSnapshot() throws Exception {
                UUID aprovacaoId = UUID.randomUUID();
                String idempotencyKey = "idempotency-create-" + UUID.randomUUID();
                UUID orgId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

                ApprovalActionPayload payload = new ApprovalActionPayload(
                                idempotencyKey,
                                null,
                                "Evento idempotente pos-aprovacao",
                                null,
                                null,
                                orgId,
                                null,
                                Instant.parse("2027-10-01T10:00:00Z"),
                                Instant.parse("2027-10-01T11:00:00Z"),
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

                // First approval attempt
                mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "CONSELHO")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                { "status": "APROVADA", "observacao": "ok" }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.actionExecution.outcome").value("EXECUTED"));

                assertThat(eventoJpaRepository.count()).isEqualTo(eventsBefore + 1);

                // Attempting to approve again must fail with APPROVAL_ALREADY_DECIDED (409)
                mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "CONSELHO")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                { "status": "APROVADA", "observacao": "duplicado" }
                                                """))
                                .andExpect(status().isConflict());

                // Only one event created
                assertThat(eventoJpaRepository.count()).isEqualTo(eventsBefore + 1);
        }
}
