package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class CreateEventoAprovacaoAuditTrailIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void devePersistirAprovacaoLinhaTempoParaCriarExecucao() throws Exception {
        long eventCountBefore = eventoJpaRepository.count();

        MvcResult pendenteResult = mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "secretario")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000cc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Evento trilha create",
                                  "grupoParoquialResponsavelId": "00000000-0000-0000-0000-0000000000cc",
                                  "inicio": "2027-03-20T10:00:00Z",
                                  "fim": "2027-03-20T11:00:00Z",
                                  "adicionadoExtraJustificativa": "Justificativa formal do secretario"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        UUID aprovacaoId = UUID.fromString(objectMapper.readTree(pendenteResult.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asString());

        var pendente = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        assertThat(pendente.getStatus()).isEqualTo(AprovacaoStatus.PENDENTE);
        assertThat(pendente.getCriadoEmUtc()).isNotNull();
        assertThat(pendente.getDecididoEmUtc()).isNull();
        assertThat(pendente.getExecutadoEmUtc()).isNull();
        assertThat(pendente.getActionPayloadJson()).contains("Evento trilha create");

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APROVADA",
                                  "observacao": "ok"
                                }
                                """))
                .andExpect(status().isOk());

        var decided = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        assertThat(decided.getStatus()).isEqualTo(AprovacaoStatus.APROVADA);
        assertThat(decided.getDecididoEmUtc()).isNotNull();
        assertThat(decided.getExecutadoEmUtc()).isNotNull();
        assertThat(decided.getEventoId()).isNotNull();
        assertThat(eventoJpaRepository.count()).isEqualTo(eventCountBefore + 1);
    }
}
