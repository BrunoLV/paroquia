package br.com.nsfatima.gestao.calendario.integration.eventos;

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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.test.context.jdbc.Sql;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class AprovacaoFlowRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void deveValidarCoreAprovacaoRegressionScenarios() throws Exception {
        long eventsBefore = eventoJpaRepository.count();

        MvcResult createPendente = mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "secretario-conselho")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000dd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Suite create approve",
                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000dd",
                                  "inicio": "2027-08-01T10:00:00Z",
                                  "fim": "2027-08-01T11:00:00Z",
                                  "adicionadoExtraJustificativa": "Justificativa formal do secretario"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        UUID createAprovacaoId = UUID.fromString(
                objectMapper.readTree(createPendente.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asString());

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", createAprovacaoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APROVADA"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionExecution.outcome").value("EXECUTED"));

        MvcResult immediateCreate = mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000dd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Suite update reject",
                                  "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000dd",
                                  "inicio": "2027-08-10T10:00:00Z",
                                  "fim": "2027-08-10T11:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        UUID eventId = UUID.fromString(objectMapper.readTree(immediateCreate.getResponse().getContentAsString())
                .get("id").asString());

        MvcResult updatePendente = mockMvc.perform(patch("/api/v1/eventos/{id}", eventId)
                        .header("X-Actor-Role", "coordenador")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000dd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inicio": "2027-08-15T10:00:00Z",
                                  "fim": "2027-08-15T11:00:00Z"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        UUID updateAprovacaoId = UUID.fromString(
                objectMapper.readTree(updatePendente.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asString());

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", updateAprovacaoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "REPROVADA",
                                  "observacao": "Reprovado por falta de espaço"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.actionExecution.outcome").value("REJECTED"));

        var unchanged = eventoJpaRepository.findById(eventId).orElseThrow();
        assertThat(unchanged.getInicioUtc().toString()).contains("2027-08-10");

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", createAprovacaoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APROVADA"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("APROVACAO_JA_DECIDIDA"));

        assertThat(eventoJpaRepository.count()).isGreaterThanOrEqualTo(eventsBefore + 2);
        assertThat(aprovacaoJpaRepository.findById(createAprovacaoId)).isPresent();
        assertThat(aprovacaoJpaRepository.findById(updateAprovacaoId)).isPresent();
    }
}
