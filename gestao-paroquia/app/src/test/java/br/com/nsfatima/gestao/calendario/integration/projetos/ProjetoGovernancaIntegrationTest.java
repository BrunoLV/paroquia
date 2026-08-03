package br.com.nsfatima.gestao.calendario.integration.projetos;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoSolicitacaoAprovacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class ProjetoGovernancaIntegrationTest {

    private static final String PASTORAL_ORG = "00000000-0000-0000-0000-0000000000aa";
    private static final String CONSELHO_ORG = "00000000-0000-0000-0000-0000000000cc";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProjetoEventoJpaRepository repository;
    @Autowired
    private ProjetoSolicitacaoAprovacaoJpaRepository approvalRepository;
    @Autowired
    private AnoParoquialJpaRepository anoParoquialRepository;
    @Autowired
    private EventoJpaRepository eventRepository;
    private int testYear;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        repository.deleteAll();
        approvalRepository.deleteAll();
        anoParoquialRepository.deleteAll();
        testYear = LocalDate.now().getYear();
    }

    @Test
    @DisplayName("Should create project directly in open year (EM_PLANEJAMENTO)")
    void deveCriarImediatamenteEmAbertoAno() throws Exception {
        saveAnoParoquial(testYear, AnoParoquialStatus.EM_PLANEJAMENTO);

        String payload = """
                {
                  "nome": "Projeto Ano Aberto",
                  "descricao": "Teste em ano aberto",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "%d-06-01T00:00:00Z",
                  "fim": "%d-06-30T23:59:59Z"
                }
                """.formatted(CONSELHO_ORG, testYear, testYear);

        mockMvc.perform(post("/api/v1/projetos")
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .header("X-Actor-Org-Id", CONSELHO_ORG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Projeto Ano Aberto"))
                .andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    @DisplayName("Should return 202 Accepted and create approval request when creating project in closed year")
    void deveRotaParaAprovacaoQuandoAnoFechado() throws Exception {
        saveAnoParoquial(testYear, AnoParoquialStatus.FECHADO);

        String payload = """
                {
                  "nome": "Projeto Ano Fechado",
                  "descricao": "Teste em ano fechado",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "%d-06-01T00:00:00Z",
                  "fim": "%d-06-30T23:59:59Z"
                }
                """.formatted(CONSELHO_ORG, testYear, testYear);

        mockMvc.perform(post("/api/v1/projetos")
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .header("X-Actor-Org-Id", CONSELHO_ORG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.tipoSolicitacao").value("CRIACAO"))
                .andExpect(jsonPath("$.actionPayloadJson").isNotEmpty());
    }

    @Test
    @DisplayName("Should start project manually when authorized")
    void deveIniciarProjetoManualmenteQuandoAutorizado() throws Exception {
        ProjetoEventoEntity project = saveProject("Projeto Aberto", ProjetoStatus.ABERTO, CONSELHO_ORG);

        mockMvc.perform(post("/api/v1/projetos/{id}/iniciar", project.getId())
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .header("X-Actor-Org-Id", CONSELHO_ORG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    @DisplayName("Should reject manual start when unauthorized role/org")
    void deveRejeitarManualIniciarQuandoNaoAutorizado() throws Exception {
        ProjetoEventoEntity project = saveProject("Projeto Aberto", ProjetoStatus.ABERTO, CONSELHO_ORG);

        mockMvc.perform(post("/api/v1/projetos/{id}/iniciar", project.getId())
                        .header("X-Actor-Role", "coordenador-pastoral-local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", PASTORAL_ORG)) // different org
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should conclude project manually when authorized")
    void deveConcluirProjetoManualmenteQuandoAutorizado() throws Exception {
        ProjetoEventoEntity project = saveProject("Projeto Em Andamento", ProjetoStatus.EM_ANDAMENTO, CONSELHO_ORG);

        mockMvc.perform(post("/api/v1/projetos/{id}/concluir", project.getId())
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .header("X-Actor-Org-Id", CONSELHO_ORG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDO"));
    }

    @Test
    @DisplayName("Should cancel project immediately when requested by Council Leader")
    void deveCancelarImediatamenteQuandoSolicitadoPorConselhoLider() throws Exception {
        ProjetoEventoEntity project = saveProject("Projeto Aberto", ProjetoStatus.ABERTO, CONSELHO_ORG);

        mockMvc.perform(post("/api/v1/projetos/{id}/cancelar", project.getId())
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .header("X-Actor-Org-Id", CONSELHO_ORG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cancelarEventosVinculados\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    @DisplayName("Should route to approval cancellation request when requested by Pastoral Coordinator")
    void deveRotaParaAprovacaoCancelamentoQuandoSolicitadoPorPastoralCoordenador() throws Exception {
        ProjetoEventoEntity project = saveProject("Projeto Aberto", ProjetoStatus.ABERTO, PASTORAL_ORG);

        mockMvc.perform(post("/api/v1/projetos/{id}/cancelar", project.getId())
                        .header("X-Actor-Role", "coordenador-pastoral-local")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", PASTORAL_ORG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cancelarEventosVinculados\":true}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.tipoSolicitacao").value("CANCELAMENTO"))
                .andExpect(jsonPath("$.cancelarEventosVinculados").value(true));
    }

    @Test
    @DisplayName("Should approve project creation request and create project")
    void deveAprovarProjetoCriacaoRequest() throws Exception {
        String payloadJson = """
                {
                  "nome": "Projeto Aprovado Criacao",
                  "descricao": "Payload",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-06-01T00:00:00Z",
                  "fim": "2026-06-30T23:59:59Z"
                }
                """.formatted(CONSELHO_ORG);

        ProjetoSolicitacaoAprovacaoEntity req = new ProjetoSolicitacaoAprovacaoEntity();
        req.setId(UUID.randomUUID());
        req.setTipoSolicitacao("CRIACAO");
        req.setStatus(AprovacaoStatus.PENDENTE);
        req.setSolicitanteId("solicitante");
        req.setSolicitantePapel("coordenador-pastoral-local");
        req.setSolicitanteTipoOrganizacao(TipoOrganizacao.PASTORAL);
        req.setCriadoEmUtc(Instant.now());
        req.setActionPayloadJson(payloadJson);
        approvalRepository.saveAndFlush(req);

        mockMvc.perform(post("/api/v1/projetos/solicitacoes/{id}/decisao", req.getId())
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"aprovado\":true,\"observacao\":\"Ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));

        // Verify project is created
        var projects = repository.findAll();
        assertEquals(1, projects.size());
        assertEquals("Projeto Aprovado Criacao", projects.getFirst().getNome());
        assertEquals(ProjetoStatus.ABERTO, projects.getFirst().getStatus());
    }

    @Test
    @DisplayName("Should cancel linked events when cancelling project with cascade = true")
    void deveCancelarVinculadoEventosQuandoCascataVerdadeiro() throws Exception {
        ProjetoEventoEntity project = saveProject("Projeto Cascata", ProjetoStatus.ABERTO, CONSELHO_ORG);

        EventoEntity event = new EventoEntity();
        event.setId(UUID.randomUUID());
        event.setTitulo("Evento no Projeto");
        event.setOrganizacaoResponsavelId(UUID.fromString(CONSELHO_ORG));
        event.setInicioUtc(Instant.now());
        event.setFimUtc(Instant.now().plusSeconds(3600));
        event.setStatus("CONFIRMADO");
        event.setProjetoId(project.getId());
        eventRepository.saveAndFlush(event);

        mockMvc.perform(post("/api/v1/projetos/{id}/cancelar", project.getId())
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .header("X-Actor-Org-Id", CONSELHO_ORG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cancelarEventosVinculados\":true}"))
                .andExpect(status().isOk());

        // Verify project is CANCELADO
        var savedProject = repository.findById(project.getId()).get();
        assertEquals(ProjetoStatus.CANCELADO, savedProject.getStatus());

        // Verify linked event is CANCELADO
        var savedEvent = eventRepository.findById(event.getId()).get();
        assertEquals("CANCELADO", savedEvent.getStatus());
        assertEquals(project.getId(), savedEvent.getProjetoId());
    }

    @Test
    @DisplayName("Should unlink events when cancelling project with cascade = false")
    void deveDesvincularEventosQuandoCascataFalso() throws Exception {
        ProjetoEventoEntity project = saveProject("Projeto Sem Cascata", ProjetoStatus.ABERTO, CONSELHO_ORG);

        EventoEntity event = new EventoEntity();
        event.setId(UUID.randomUUID());
        event.setTitulo("Evento Solto");
        event.setOrganizacaoResponsavelId(UUID.fromString(CONSELHO_ORG));
        event.setInicioUtc(Instant.now());
        event.setFimUtc(Instant.now().plusSeconds(3600));
        event.setStatus("CONFIRMADO");
        event.setProjetoId(project.getId());
        eventRepository.saveAndFlush(event);

        mockMvc.perform(post("/api/v1/projetos/{id}/cancelar", project.getId())
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .header("X-Actor-Org-Id", CONSELHO_ORG)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cancelarEventosVinculados\":false}"))
                .andExpect(status().isOk());

        // Verify project is CANCELADO
        var savedProject = repository.findById(project.getId()).get();
        assertEquals(ProjetoStatus.CANCELADO, savedProject.getStatus());

        // Verify linked event is NOT CANCELADO but project association is removed
        var savedEvent = eventRepository.findById(event.getId()).get();
        assertEquals("CONFIRMADO", savedEvent.getStatus());
        assertNull(savedEvent.getProjetoId());
    }

    private void saveAnoParoquial(int ano, AnoParoquialStatus status) {
        AnoParoquialEntity entity = new AnoParoquialEntity();
        entity.setAno(ano);
        entity.setStatus(status);
        anoParoquialRepository.saveAndFlush(entity);
    }

    private ProjetoEventoEntity saveProject(String nome, ProjetoStatus status, String orgId) {
        ProjetoEventoEntity project = new ProjetoEventoEntity();
        project.setId(UUID.randomUUID());
        project.setNome(nome);
        project.setDescricao("Teste");
        project.setOrganizacaoResponsavelId(UUID.fromString(orgId));
        project.setStatus(status);
        project.setInicioUtc(Instant.now());
        project.setFimUtc(Instant.now().plusSeconds(86400));
        return repository.saveAndFlush(project);
    }
}

