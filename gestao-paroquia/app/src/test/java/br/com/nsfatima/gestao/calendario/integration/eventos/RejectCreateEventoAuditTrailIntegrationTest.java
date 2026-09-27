package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class RejectCreateEventoAuditTrailIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    @Autowired
    private CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;

    @Test
    @SuppressWarnings("null")
    void rejectedCreateAprovacaoLeavesNoOrphanAndHasCorrectAuditState() throws Exception {
        UUID aprovacaoId = UUID.randomUUID();
        UUID orgId = UUID.fromString("00000000-0000-0000-0000-0000000000bb");

        AprovacaoAcaoPayload payload = new AprovacaoAcaoPayload(
                UUID.randomUUID().toString(),
                null,
                "Auditoria de reprovacao",
                null,
                null,
                orgId,
                null,
                Instant.parse("2027-12-01T10:00:00Z"),
                Instant.parse("2027-12-01T11:00:00Z"),
                null,
                null,
                null,
                null,
                null,
                null);

        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(aprovacaoId);
        aprovacao.setEventoId(null);
        aprovacao.setTipoSolicitacao("CRIACAO_EVENTO");
        aprovacao.setAprovadorPapel("paroco");
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setActionPayloadJson(aprovacaoAcaoPayloadMapper.toJson(payload));
        aprovacaoJpaRepository.save(aprovacao);

        long eventsBefore = eventoJpaRepository.count();

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "REPROVADA",
                                  "observacao": "Nao se enquadra ao calendario"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REPROVADA"))
                .andExpect(jsonPath("$.actionExecution.outcome").value("REJECTED"));

        // No event created
        assertThat(eventoJpaRepository.count()).isEqualTo(eventsBefore);

        // Aprovacao state is consistently REPROVADA with no eventoId
        AprovacaoEntity saved = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(AprovacaoStatus.REPROVADA);
        assertThat(saved.getEventoId()).isNull();
        assertThat(saved.getDecididoEmUtc()).isNotNull();
        assertThat(saved.getExecutadoEmUtc()).isNull();
        assertThat(saved.getDecisionObservacao()).isEqualTo("Nao se enquadra ao calendario");
    }
}
