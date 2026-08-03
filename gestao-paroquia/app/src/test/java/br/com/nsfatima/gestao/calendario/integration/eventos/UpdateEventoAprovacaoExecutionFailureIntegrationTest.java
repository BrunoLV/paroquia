package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class UpdateEventoAprovacaoExecutionFailureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Autowired
    private AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

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
                objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asString());

        // Setup aprovacao with invalid/non-existent eventoId in payload
        UUID aprovacaoId = UUID.randomUUID();
        UUID nonExistentEventoId = UUID.randomUUID();

        AprovacaoAcaoPayload payload = new AprovacaoAcaoPayload(
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
                null,
                null);

        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(aprovacaoId);
        aprovacao.setEventoId(nonExistentEventoId);
        aprovacao.setTipoSolicitacao("EDICAO_EVENTO");
        aprovacao.setAprovadorPapel("conselho-coordenador");
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setSolicitanteId(UUID.randomUUID().toString());
        aprovacao.setSolicitantePapel("coordenador-pastoral-local");
        aprovacao.setSolicitanteTipoOrganizacao(TipoOrganizacao.PASTORAL);
        aprovacao.setCorrelationId(aprovacaoId.toString());
        aprovacao.setActionPayloadJson(aprovacaoAcaoPayloadMapper.toJson(payload));
        aprovacaoJpaRepository.save(aprovacao);

        long eventoCountBefore = eventoJpaRepository.count();

        // Approve -> execution fails (evento not found) -> 409 with deterministic code
        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APROVADA",
                                  "observacao": "Tentativa de execucao"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("APROVACAO_EXECUCAO_FALHOU"));

        // Original event unchanged
        assertThat(eventoJpaRepository.count()).isEqualTo(eventoCountBefore);
        var evento = eventoJpaRepository.findById(eventoId).orElseThrow();
        assertThat(evento.getInicioUtc().toString()).contains("2027-11-01");

        // Aprovacao persisted as FALHA_EXECUCAO
        var savedAprovacao = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        assertThat(savedAprovacao.getStatus()).isEqualTo(AprovacaoStatus.FALHA_EXECUCAO);
        assertThat(savedAprovacao.getExecutadoEmUtc()).isNull();
    }
}
