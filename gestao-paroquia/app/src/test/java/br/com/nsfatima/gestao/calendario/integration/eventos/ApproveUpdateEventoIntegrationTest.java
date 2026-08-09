package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
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
class ApproveUpdateEventoIntegrationTest {

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
    void deveAutoAtualizarEventoDepoisAprovacao() throws Exception {
        // Step 1: Create event (paroco → immediate 201)
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Evento original",
                                  "grupoParoquialResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                  "inicio": "2027-08-01T10:00:00Z",
                                  "fim": "2027-08-01T11:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        UUID eventoId = UUID.fromString(
                objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asString());

        // Step 2: PATCH with sensitive fields by coordenador/PASTORAL → 202 PENDENTE
        MvcResult patchResult = mockMvc.perform(patch("/api/v1/eventos/{id}", eventoId)
                        .header("X-Actor-Role", "coordenador-pastoral-local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inicio": "2027-08-05T10:00:00Z",
                                  "fim": "2027-08-05T11:00:00Z"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        UUID aprovacaoId = UUID.fromString(
                objectMapper.readTree(patchResult.getResponse().getContentAsString())
                        .get("solicitacaoAprovacaoId").asString());

        // Event should not be changed yet
        var eventoBeforeAprovacao = eventoJpaRepository.findById(eventoId).orElseThrow();
        assertThat(eventoBeforeAprovacao.getInicioUtc().toString()).contains("2027-08-01");

        // Step 3: PATCH approve → update applied automatically
        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APROVADA",
                                  "observacao": "Aprovado"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.actionExecution.outcome").value("EXECUTED"))
                .andExpect(jsonPath("$.actionExecution.eventoId").value(eventoId.toString()));

        // Event should now have updated fields
        var eventoAfterAprovacao = eventoJpaRepository.findById(eventoId).orElseThrow();
        assertThat(eventoAfterAprovacao.getInicioUtc().toString()).contains("2027-08-05");

        // Aprovacao should have executadoEmUtc set
        var savedAprovacao = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        assertThat(savedAprovacao.getExecutadoEmUtc()).isNotNull();
    }

    @Test
    @SuppressWarnings("null")
    void deveAplicarAtualizarDeDiretoPendenteSnapshot() throws Exception {
        // Create event first
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Evento snapshot direto",
                                  "grupoParoquialResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                  "inicio": "2027-09-01T09:00:00Z",
                                  "fim": "2027-09-01T10:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        UUID eventoId = UUID.fromString(
                objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asString());

        // Setup aprovacao directly
        UUID aprovacaoId = UUID.randomUUID();
        AprovacaoAcaoPayload payload = new AprovacaoAcaoPayload(
                null,
                eventoId,
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2027-09-10T09:00:00Z"),
                Instant.parse("2027-09-10T10:00:00Z"),
                null,
                null,
                null,
                null,
                null,
                null);

        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(aprovacaoId);
        aprovacao.setEventoId(eventoId);
        aprovacao.setTipoSolicitacao("EDICAO_EVENTO");
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setSolicitanteId(UUID.randomUUID().toString());
        aprovacao.setSolicitantePapel("coordenador-pastoral-local");
        aprovacao.setSolicitanteTipoGrupo(TipoGrupo.PASTORAL);
        aprovacao.setCorrelationId(aprovacaoId.toString());
        aprovacao.setAprovadorPapel("conselho-coordenador");
        aprovacao.setActionPayloadJson(aprovacaoAcaoPayloadMapper.toJson(payload));
        aprovacaoJpaRepository.save(aprovacao);

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APROVADA",
                                  "observacao": "Aprovado snapshot"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionExecution.outcome").value("EXECUTED"))
                .andExpect(jsonPath("$.actionExecution.eventoId").value(eventoId.toString()));

        var eventoAfter = eventoJpaRepository.findById(eventoId).orElseThrow();
        assertThat(eventoAfter.getInicioUtc().toString()).contains("2027-09-10");
    }
}
