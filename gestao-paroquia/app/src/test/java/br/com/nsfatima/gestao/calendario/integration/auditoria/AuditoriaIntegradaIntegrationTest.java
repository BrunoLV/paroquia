package br.com.nsfatima.gestao.calendario.integration.auditoria;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditoriaIntegradaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditoriaOperacaoJpaRepository auditoriaRepository;

    @Autowired
    private ProjetoEventoJpaRepository projetoRepository;

    @Autowired
    private AprovacaoJpaRepository aprovacaoRepository;

    @Autowired
    private EventoJpaRepository eventoRepository;

    private static final String ORG_ID = "00000000-0000-0000-0000-0000000000aa";

    @BeforeEach
    void setUp() {
        auditoriaRepository.deleteAll();
        projetoRepository.deleteAll();
        aprovacaoRepository.deleteAll();
        eventoRepository.deleteAll();
    }

    @Test
    @DisplayName("Should audit project creation and update")
    void shouldAuditProjectOperations() throws Exception {
        // 1. Create Project
        String createResponse = mockMvc.perform(post("/api/v1/projetos")
                        .header("X-Actor-Role", "coordenador")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"nome\":\"Projeto Auditado\"," +
                                "\"descricao\":\"Teste\"," +
                                "\"organizacaoResponsavelId\":\"" + ORG_ID + "\"," +
                                "\"inicio\":\"" + Instant.now() + "\"," +
                                "\"fim\":\"" + Instant.now().plusSeconds(3600) + "\"" +
                                "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String projectId = JsonPath.read(createResponse, "$.id");

        // 2. Patch Project
        mockMvc.perform(patch("/api/v1/projetos/{id}", projectId)
                        .header("X-Actor-Role", "coordenador")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Projeto Auditado Atualizado\"}"))
                .andExpect(status().isOk());

        // 3. Verify Audit Trail
        mockMvc.perform(get("/api/v1/auditoria/eventos/trilha")
                        .param("organizacaoId", ORG_ID)
                        .param("granularidade", "MENSAL")
                        .header("X-Actor-Org-Id", ORG_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.recursoTipo == 'PROJETO' && @.acao == 'create')]").exists())
                .andExpect(jsonPath("$.items[?(@.recursoTipo == 'PROJETO' && @.acao == 'patch')]").exists());
    }

    @Test
    @DisplayName("Should audit approval decisions")
    void shouldAuditApprovalDecisions() throws Exception {
        // Create an event to satisfy organization resolution
        EventoEntity evento = new EventoEntity();
        evento.setId(UUID.randomUUID());
        evento.setTitulo("Evento para Auditoria");
        evento.setInicioUtc(Instant.now());
        evento.setFimUtc(Instant.now().plusSeconds(3600));
        evento.setOrganizacaoResponsavelId(UUID.fromString(ORG_ID));
        evento.setStatus("RASCUNHO");
        eventoRepository.save(evento);

        UUID approvalId = UUID.randomUUID();
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(approvalId);
        aprovacao.setEventoId(evento.getId());
        aprovacao.setTipoSolicitacao("ALTERACAO_HORARIO");
        aprovacao.setAprovadorPapel("conselho-coordenador");
        aprovacao.setStatus("PENDENTE");
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setCorrelationId("decide-audit-test");
        aprovacaoRepository.save(aprovacao);

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", approvalId)
                        .header("X-Actor-Role", "coordenador")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"REPROVADA\",\"observacao\":\"Auditando decisao\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/auditoria/eventos/trilha")
                        .param("organizacaoId", ORG_ID)
                        .param("granularidade", "MENSAL")
                        .header("X-Actor-Org-Id", ORG_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[?(@.recursoTipo == 'APROVACAO' && @.acao == 'approval-decision')]").exists());
    }
}
