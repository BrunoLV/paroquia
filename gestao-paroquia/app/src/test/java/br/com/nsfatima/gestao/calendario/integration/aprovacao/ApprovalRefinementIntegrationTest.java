package br.com.nsfatima.gestao.calendario.integration.aprovacao;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayload;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
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
class ApprovalRefinementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AprovacaoJpaRepository aprovacaoRepository;

    @Autowired
    private ApprovalActionPayloadMapper payloadMapper;

    private static final String ORG_ID = "00000000-0000-0000-0000-0000000000aa";

    @Test
    void shouldRejectExpiredApprovalRequest() throws Exception {
        UUID approvalId = UUID.randomUUID();
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(approvalId);
        aprovacao.setEventoId(UUID.randomUUID());
        aprovacao.setTipoSolicitacao(TipoSolicitacaoInput.EDICAO_EVENTO.name());
        aprovacao.setAprovadorPapel("conselho-coordenador");
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        
        // Payload with past date
        ApprovalActionPayload payload = new ApprovalActionPayload(
                null, aprovacao.getEventoId(), "Exp", "Desc", null, UUID.fromString(ORG_ID),
                null, Instant.now().minusSeconds(3600), Instant.now().minusSeconds(1800),
                null, null, null, null, null);
        aprovacao.setActionPayloadJson(payloadMapper.toJson(payload));
        aprovacaoRepository.save(aprovacao);

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", approvalId)
                        .header("X-Actor-Role", "coordenador")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APROVADA\",\"observacao\":\"Tentando aprovar expirado\"}"))
                .andExpect(status().isConflict()) // ApprovalExecutionFailedException maps to 409
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Approval request has expired (event already started at")));
        }
        }

