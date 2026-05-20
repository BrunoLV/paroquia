package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class UpdateEventoApprovalPendingIntegrationTest {

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
    void coordenadorPastoralSensitiveFieldsReturnsPendingApproval() throws Exception {
        // Create event first (paroco/CLERO → immediate 201)
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento para editar",
                          "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                          "inicio": "2027-06-01T10:00:00Z",
                          "fim": "2027-06-01T11:00:00Z"
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        String eventoIdStr = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();
        UUID eventoId = UUID.fromString(eventoIdStr);

        long aprovacoesBefore = aprovacaoJpaRepository.count();

        // PATCH with sensitive fields by coordenador/PASTORAL → 202 PENDING
        MvcResult patchResult = mockMvc.perform(patch("/api/v1/eventos/{id}", eventoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "inicio": "2027-06-02T10:00:00Z",
                          "fim": "2027-06-02T11:00:00Z"
                        }
                        """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.solicitacaoAprovacaoId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.mensagem").value("APPROVAL_PENDING"))
                .andReturn();

        assertThat(aprovacaoJpaRepository.count()).isEqualTo(aprovacoesBefore + 1);

        String aprovacaoIdStr = objectMapper.readTree(patchResult.getResponse().getContentAsString())
                .get("solicitacaoAprovacaoId").asText();
        var aprovacao = aprovacaoJpaRepository.findById(UUID.fromString(aprovacaoIdStr)).orElseThrow();
        System.err.println("DEBUG ACTUAL ROLE: [" + aprovacao.getSolicitantePapel() + "]");
        assertThat(aprovacao.getEventoId()).isEqualTo(eventoId);
        assertThat(aprovacao.getTipoSolicitacao()).isEqualTo("EDICAO_EVENTO");
        assertThat(aprovacao.getStatus()).isEqualTo("PENDENTE");
        assertThat(aprovacao.getActionPayloadJson()).contains("2027-06-02");
        assertThat(aprovacao.getSolicitantePapel()).isEqualTo("coordenador");
        assertThat(aprovacao.getSolicitanteTipoOrganizacao()).isEqualTo("PASTORAL");

        // Event should not be modified yet
        var evento = eventoJpaRepository.findById(eventoId).orElseThrow();
        assertThat(evento.getInicioUtc().toString()).contains("2027-06-01");
    }

    @Test
    @SuppressWarnings("null")
    void viceCoordenadorLaicatoSensitiveFieldsReturnsPendingApproval() throws Exception {
        // Create event first
        MvcResult createResult = mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID())
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento laicato",
                          "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000aa",
                          "inicio": "2027-07-01T09:00:00Z",
                          "fim": "2027-07-01T10:00:00Z"
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        String eventoIdStr = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mockMvc.perform(patch("/api/v1/eventos/{id}", eventoIdStr)
                .header("X-Actor-Role", "vice-coordenador")
                .header("X-Actor-Org-Type", "LAICATO")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "fim": "2027-07-01T11:00:00Z"
                        }
                        """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.mensagem").value("APPROVAL_PENDING"));
    }
}
