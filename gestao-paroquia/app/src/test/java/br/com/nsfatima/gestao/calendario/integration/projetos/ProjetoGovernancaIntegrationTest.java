package br.com.nsfatima.gestao.calendario.integration.projetos;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoSolicitacaoAprovacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProjetoGovernancaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoEventoJpaRepository repository;

    @Autowired
    private ProjetoSolicitacaoAprovacaoJpaRepository approvalRepository;

    @Autowired
    private AnoParoquialJpaRepository anoParoquialRepository;

    private int testYear;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        approvalRepository.deleteAll();
        anoParoquialRepository.deleteAll();
        testYear = LocalDate.now().getYear();
    }

    @Test
    @DisplayName("Should create project directly in open year (EM_PLANEJAMENTO)")
    void shouldCreateImmediatelyInOpenYear() throws Exception {
        saveAnoParoquial(testYear, AnoParoquialStatus.EM_PLANEJAMENTO);
        String orgId = UUID.randomUUID().toString();

        String payload = """
                {
                  "nome": "Projeto Ano Aberto",
                  "descricao": "Teste em ano aberto",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "%d-06-01T00:00:00Z",
                  "fim": "%d-06-30T23:59:59Z"
                }
                """.formatted(orgId, testYear, testYear);

        mockMvc.perform(post("/api/v1/projetos")
                        .header("X-Actor-Role", "coordenador")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Projeto Ano Aberto"))
                .andExpect(jsonPath("$.status").value("ABERTO"));
    }

    @Test
    @DisplayName("Should return 202 Accepted and create approval request when creating project in closed year")
    void shouldRouteToApprovalWhenYearClosed() throws Exception {
        saveAnoParoquial(testYear, AnoParoquialStatus.FECHADO);
        String orgId = UUID.randomUUID().toString();

        String payload = """
                {
                  "nome": "Projeto Ano Fechado",
                  "descricao": "Teste em ano fechado",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "%d-06-01T00:00:00Z",
                  "fim": "%d-06-30T23:59:59Z"
                }
                """.formatted(orgId, testYear, testYear);

        mockMvc.perform(post("/api/v1/projetos")
                        .header("X-Actor-Role", "coordenador")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.tipoSolicitacao").value("CRIACAO"))
                .andExpect(jsonPath("$.actionPayloadJson").isNotEmpty());
    }

    private void saveAnoParoquial(int ano, AnoParoquialStatus status) {
        AnoParoquialEntity entity = new AnoParoquialEntity();
        entity.setAno(ano);
        entity.setStatus(status.name());
        anoParoquialRepository.saveAndFlush(entity);
    }
}
