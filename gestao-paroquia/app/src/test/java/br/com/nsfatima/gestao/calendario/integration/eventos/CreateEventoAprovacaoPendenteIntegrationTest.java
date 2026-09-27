package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class CreateEventoAprovacaoPendenteIntegrationTest {

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
    void secretarioRoleReturnsPendenteAprovacao() throws Exception {
        long eventsBefore = eventoJpaRepository.count();

        MvcResult result = mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "secretario-conselho")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Missa pendente de aprovacao",
                                  "grupoParoquialResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                  "inicio": "2027-03-10T10:00:00Z",
                                  "fim": "2027-03-10T11:00:00Z",
                                  "adicionadoExtraJustificativa": "Justificativa formal do secretario"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.solicitacaoAprovacaoId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.mensagem").value("APROVACAO_PENDENTE"))
                .andReturn();

        String aprovacaoIdStr = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asString();
        var aprovacao = aprovacaoJpaRepository.findById(UUID.fromString(aprovacaoIdStr)).orElseThrow();
        assertThat(aprovacao.getEventoId()).isNull();
        assertThat(aprovacao.getActionPayloadJson()).contains("Missa pendente de aprovacao");
        assertThat(aprovacao.getSolicitantePapel()).isEqualTo("secretario-conselho");
        assertThat(aprovacao.getSolicitanteTipoGrupo()).isEqualTo(TipoGrupo.CONSELHO);
    }

    @Test
    void secretarioSemJustificativaDeveRetornar400() throws Exception {
        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "secretario-conselho")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Sem justificativa",
                                  "grupoParoquialResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                                  "inicio": "2027-03-10T10:00:00Z",
                                  "fim": "2027-03-10T11:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("DOMAIN_RULE_VIOLATION"));
    }

    @Test
    void coordenadorPastoralRoleReturnsPendenteAprovacao() throws Exception {
        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID())
                        .header("X-Actor-Role", "coordenador")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000bb")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Culto pastoral pendente",
                                  "grupoParoquialResponsavelId": "00000000-0000-0000-0000-0000000000bb",
                                  "inicio": "2027-03-11T14:00:00Z",
                                  "fim": "2027-03-11T15:00:00Z"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.mensagem").value("APROVACAO_PENDENTE"));
    }
}
