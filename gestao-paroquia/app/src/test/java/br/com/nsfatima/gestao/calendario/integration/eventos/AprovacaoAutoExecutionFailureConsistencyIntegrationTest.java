package br.com.nsfatima.gestao.calendario.integration.eventos;

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

import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class AprovacaoAutoExecutionFailureConsistencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void createFailureKeepsStateConsistentAndReturnsDeterministicError() throws Exception {
        long countBefore = eventoJpaRepository.count();

        UUID aprovacaoId = UUID.randomUUID();
        AprovacaoAcaoPayload invalidPayload = new AprovacaoAcaoPayload(
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
                null,
                null);

        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(aprovacaoId);
        aprovacao.setTipoSolicitacao("CRIACAO_EVENTO");
        aprovacao.setAprovadorPapel("paroco");
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setSolicitanteId(UUID.randomUUID().toString());
        aprovacao.setSolicitantePapel("secretario");
        aprovacao.setSolicitanteTipoOrganizacao(TipoOrganizacao.CONSELHO);
        aprovacao.setCorrelationId(aprovacaoId.toString());
        aprovacao.setActionPayloadJson(aprovacaoAcaoPayloadMapper.toJson(invalidPayload));
        aprovacaoJpaRepository.save(aprovacao);

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APROVADA"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("APROVACAO_EXECUCAO_FALHOU"));

        assertThat(eventoJpaRepository.count()).isEqualTo(countBefore);
        var saved = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(AprovacaoStatus.FALHA_EXECUCAO);
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
                        .get("id").asString());
        long countBefore = eventoJpaRepository.count();

        UUID aprovacaoId = UUID.randomUUID();
        UUID nonexistentEventId = UUID.randomUUID();

        AprovacaoAcaoPayload invalidPayload = new AprovacaoAcaoPayload(
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
                null,
                null);

        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(aprovacaoId);
        aprovacao.setEventoId(nonexistentEventId);
        aprovacao.setTipoSolicitacao("EDICAO_EVENTO");
        aprovacao.setAprovadorPapel("paroco");
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setSolicitanteId(UUID.randomUUID().toString());
        aprovacao.setSolicitantePapel("coordenador");
        aprovacao.setSolicitanteTipoOrganizacao(TipoOrganizacao.PASTORAL);
        aprovacao.setCorrelationId(aprovacaoId.toString());
        aprovacao.setActionPayloadJson(aprovacaoAcaoPayloadMapper.toJson(invalidPayload));
        aprovacaoJpaRepository.save(aprovacao);

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APROVADA"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("APROVACAO_EXECUCAO_FALHOU"));

        assertThat(eventoJpaRepository.count()).isEqualTo(countBefore);
        assertThat(eventoJpaRepository.findById(realEventId)).isPresent();

        var saved = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(AprovacaoStatus.FALHA_EXECUCAO);
        assertThat(saved.getMensagemErroExecucao()).isNotNull();
        assertThat(saved.getExecutadoEmUtc()).isNull();
    }
}
