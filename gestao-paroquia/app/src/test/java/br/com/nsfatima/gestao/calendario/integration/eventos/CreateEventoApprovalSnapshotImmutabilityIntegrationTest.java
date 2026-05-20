package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayload;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
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
class CreateEventoApprovalSnapshotImmutabilityIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private AprovacaoJpaRepository aprovacaoJpaRepository;

        @Autowired
        private ApprovalActionPayloadMapper approvalActionPayloadMapper;

        @Test
        @SuppressWarnings("null")
        void actionPayloadJsonIsUnchangedAfterApproval() throws Exception {
                UUID aprovacaoId = UUID.randomUUID();
                UUID orgId = UUID.fromString("00000000-0000-0000-0000-000000000001");

                ApprovalActionPayload payload = new ApprovalActionPayload(
                                UUID.randomUUID().toString(),
                                null,
                                "Snapshot imutavel",
                                null,
                                null,
                                orgId,
                                null,
                                Instant.parse("2027-11-01T10:00:00Z"),
                                Instant.parse("2027-11-01T11:00:00Z"),
                                null,
                                null,
                                null,
                                null,
                                null);

                String originalJson = approvalActionPayloadMapper.toJson(payload);

                AprovacaoEntity aprovacao = new AprovacaoEntity();
                aprovacao.setId(aprovacaoId);
                aprovacao.setEventoId(null);
                aprovacao.setTipoSolicitacao("CRIACAO_EVENTO");
                aprovacao.setAprovadorPapel("conselho-coordenador");
                aprovacao.setStatus("PENDENTE");
                aprovacao.setCriadoEmUtc(Instant.now());
                aprovacao.setActionPayloadJson(originalJson);
                aprovacaoJpaRepository.save(aprovacao);

                mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "CONSELHO")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                { "status": "APROVADA", "observacao": "ok" }
                                                """))
                                .andExpect(status().isOk());

                AprovacaoEntity saved = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
                assertThat(saved.getActionPayloadJson()).isEqualTo(originalJson);
        }

        @Test
        @SuppressWarnings("null")
        void actionPayloadJsonIsUnchangedAfterRejection() throws Exception {
                UUID aprovacaoId = UUID.randomUUID();
                UUID orgId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

                ApprovalActionPayload payload = new ApprovalActionPayload(
                                UUID.randomUUID().toString(),
                                null,
                                "Snapshot imutavel reprovado",
                                null,
                                null,
                                orgId,
                                null,
                                Instant.parse("2027-11-02T10:00:00Z"),
                                Instant.parse("2027-11-02T11:00:00Z"),
                                null,
                                null,
                                null,
                                null,
                                null);

                String originalJson = approvalActionPayloadMapper.toJson(payload);

                AprovacaoEntity aprovacao = new AprovacaoEntity();
                aprovacao.setId(aprovacaoId);
                aprovacao.setEventoId(null);
                aprovacao.setTipoSolicitacao("CRIACAO_EVENTO");
                aprovacao.setAprovadorPapel("conselho-coordenador");
                aprovacao.setStatus("PENDENTE");
                aprovacao.setCriadoEmUtc(Instant.now());
                aprovacao.setActionPayloadJson(originalJson);
                aprovacaoJpaRepository.save(aprovacao);

                mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                                .header("X-Actor-Role", "coordenador")
                                .header("X-Actor-Org-Type", "CONSELHO")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                { "status": "REPROVADA", "observacao": "nao aprovado" }
                                                """))
                                .andExpect(status().isOk());

                AprovacaoEntity saved = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
                assertThat(saved.getActionPayloadJson()).isEqualTo(originalJson);
        }
}
