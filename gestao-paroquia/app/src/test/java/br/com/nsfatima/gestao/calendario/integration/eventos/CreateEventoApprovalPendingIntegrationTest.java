package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class CreateEventoApprovalPendingIntegrationTest {

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
    void secretarioRoleReturnsPendingApproval() throws Exception {
        long eventsBefore = eventoJpaRepository.count();

        MvcResult result = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "secretario")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Missa pendente de aprovacao",
                          "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                          "inicio": "2027-03-10T10:00:00Z",
                          "fim": "2027-03-10T11:00:00Z"
                        }
                        """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.solicitacaoAprovacaoId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.mensagem").value("APPROVAL_PENDING"))
                .andReturn();

        assertThat(eventoJpaRepository.count()).isEqualTo(eventsBefore);

        String aprovacaoIdStr = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asText();
        var aprovacao = aprovacaoJpaRepository.findById(UUID.fromString(aprovacaoIdStr)).orElseThrow();
        assertThat(aprovacao.getEventoId()).isNull();
        assertThat(aprovacao.getActionPayloadJson()).contains("Missa pendente de aprovacao");
        assertThat(aprovacao.getSolicitantePapel()).isEqualTo("secretario");
        assertThat(aprovacao.getSolicitanteTipoOrganizacao()).isEqualTo("CONSELHO");
    }

    @Test
    void coordenadorPastoralRoleReturnsPendingApproval() throws Exception {
        mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000bb")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Culto pastoral pendente",
                          "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000bb",
                          "inicio": "2027-03-11T14:00:00Z",
                          "fim": "2027-03-11T15:00:00Z"
                        }
                        """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.mensagem").value("APPROVAL_PENDING"));
    }
}
