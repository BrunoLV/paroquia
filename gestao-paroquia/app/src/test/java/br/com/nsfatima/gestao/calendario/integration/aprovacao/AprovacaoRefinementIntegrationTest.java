package br.com.nsfatima.gestao.calendario.integration.aprovacao;

import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class AprovacaoRefinementIntegrationTest {

    private static final String ORG_ID = "00000000-0000-0000-0000-0000000000aa";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AprovacaoJpaRepository aprovacaoRepository;
    @Autowired
    private AprovacaoAcaoPayloadMapper payloadMapper;

    @Test
    void deveRejeitarExpiredAprovacaoRequest() throws Exception {
        UUID aprovacaoId = UUID.randomUUID();
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(aprovacaoId);
        aprovacao.setEventoId(UUID.randomUUID());
        aprovacao.setTipoSolicitacao(TipoSolicitacaoInput.EDICAO_EVENTO.name());
        aprovacao.setAprovadorPapel("conselho-coordenador");
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());

        // Payload with past date
        AprovacaoAcaoPayload payload = new AprovacaoAcaoPayload(
                null, aprovacao.getEventoId(), "Exp", "Desc", null, UUID.fromString(ORG_ID),
                null, Instant.now().minusSeconds(3600), Instant.now().minusSeconds(1800),
                null, null, null, null, null, null);
        aprovacao.setActionPayloadJson(payloadMapper.toJson(payload));
        aprovacaoRepository.save(aprovacao);

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APROVADA\",\"observacao\":\"Tentando aprovar expirado\"}"))
                .andExpect(status().isConflict()) // AprovacaoExecucaoFalhouException maps to 409
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                        "Aprovacao request has expired (event already started at")));
    }
}

