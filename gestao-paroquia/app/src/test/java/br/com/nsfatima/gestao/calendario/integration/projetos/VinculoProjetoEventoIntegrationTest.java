package br.com.nsfatima.gestao.calendario.integration.projetos;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import com.jayway.jsonpath.JsonPath;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class VinculoProjetoEventoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoEventoJpaRepository repository;

    @Autowired
    private EventoJpaRepository eventRepository;

    private static final String ORG_ID = "00000000-0000-0000-0000-0000000000aa";

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        repository.deleteAll();
    }

    @Test
    void shouldSupportProjectLinkingLifecycle() throws Exception {
        mockMvc.perform(post("/api/v1/projetos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                          "nome": "Projeto Vinculado",
                          "descricao": "Fluxo tipado",
                          "organizacaoResponsavelId": "%s",
                          "inicio": "2026-01-01T00:00:00Z",
                          "fim": "2026-12-31T23:59:59Z"
                        }
                        """.formatted(ORG_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Projeto Vinculado"));

        mockMvc.perform(get("/api/v1/projetos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Projeto Vinculado"));
    }

    @Test
    void shouldRejectEventCreationOutsideProjectPeriod() throws Exception {
        String projectResponse = mockMvc.perform(post("/api/v1/projetos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                          "nome": "Projeto com Limites",
                          "descricao": "Limites de datas",
                          "organizacaoResponsavelId": "%s",
                          "inicio": "2026-05-01T00:00:00Z",
                          "fim": "2026-05-31T23:59:59Z"
                        }
                        """.formatted(ORG_ID)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        
        String projectId = projectResponse.split("\"id\":\"")[1].split("\"")[0];

        // Event starts on June 1st (outside May)
        mockMvc.perform(post("/api/v1/eventos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                          "titulo": "Evento Fora do Periodo",
                          "descricao": "Fora das datas do projeto",
                          "categoria": "PASTORAL",
                          "organizacaoResponsavelId": "%s",
                          "projetoId": "%s",
                          "inicio": "2026-06-01T10:00:00Z",
                          "fim": "2026-06-01T11:00:00Z"
                        }
                        """.formatted(ORG_ID, projectId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAllowEventCreationWhenProjectDatesAreNull() throws Exception {
        ProjetoEventoEntity projeto = new ProjetoEventoEntity();
        projeto.setId(UUID.randomUUID());
        projeto.setNome("Projeto Sem Limites");
        projeto.setDescricao("Sem limites de datas");
        projeto.setOrganizacaoResponsavelId(UUID.fromString(ORG_ID));
        projeto.setStatus("ATIVO");
        projeto.setInicioUtc(null);
        projeto.setFimUtc(null);
        repository.save(projeto);
        
        String projectId = projeto.getId().toString();

        // Event should succeed because project dates are null
        mockMvc.perform(post("/api/v1/eventos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                          "titulo": "Evento Qualquer Data",
                          "descricao": "Projeto sem limites de data",
                          "categoria": "PASTORAL",
                          "organizacaoResponsavelId": "%s",
                          "projetoId": "%s",
                          "inicio": "2026-06-01T10:00:00Z",
                          "fim": "2026-06-01T11:00:00Z"
                        }
                        """.formatted(ORG_ID, projectId)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void shouldRejectRecurringEventRuleMismatchedDates() throws Exception {
        String projectResponse = mockMvc.perform(post("/api/v1/projetos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                          "nome": "Projeto com Recorrencia",
                          "descricao": "Limites de recorrencia",
                          "organizacaoResponsavelId": "%s",
                          "inicio": "2026-05-01T00:00:00Z",
                          "fim": "2026-05-31T23:59:59Z"
                        }
                        """.formatted(ORG_ID)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String projectId = JsonPath.read(projectResponse, "$.id");

        // Event inside project timeframe
        String eventResponse = mockMvc.perform(post("/api/v1/eventos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                          "titulo": "Base Recorrente",
                          "descricao": "Para recorrencia",
                          "categoria": "PASTORAL",
                          "organizacaoResponsavelId": "%s",
                          "projetoId": "%s",
                          "inicio": "2026-05-10T10:00:00Z",
                          "fim": "2026-05-10T11:00:00Z"
                        }
                        """.formatted(ORG_ID, projectId)))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();

        String eventId = JsonPath.read(eventResponse, "$.id");

        // 1. Recurrence rule with no end limits (indefinite) should be rejected
        mockMvc.perform(put("/api/v1/eventos/{id}/recorrencia", eventId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "frequencia": "SEMANAL",
                            "intervalo": 1,
                            "diasDaSemana": ["SUNDAY"]
                        }
                        """))
                .andExpect(status().isBadRequest());

        // 2. Recurrence rule with dataLimite exceeding project end date (2026-05-31) should be rejected
        mockMvc.perform(put("/api/v1/eventos/{id}/recorrencia", eventId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "frequencia": "SEMANAL",
                            "intervalo": 1,
                            "diasDaSemana": ["SUNDAY"],
                            "dataLimite": "2026-06-01"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAllowRecurringEventRuleMatchingDates() throws Exception {
        String projectResponse = mockMvc.perform(post("/api/v1/projetos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                          "nome": "Projeto com Recorrencia Ok",
                          "descricao": "Limites de recorrencia ok",
                          "organizacaoResponsavelId": "%s",
                          "inicio": "2026-05-01T00:00:00Z",
                          "fim": "2026-05-31T23:59:59Z"
                        }
                        """.formatted(ORG_ID)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String projectId = JsonPath.read(projectResponse, "$.id");

        // Event inside project timeframe
        String eventResponse = mockMvc.perform(post("/api/v1/eventos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                          "titulo": "Base Recorrente Ok",
                          "descricao": "Para recorrencia ok",
                          "categoria": "PASTORAL",
                          "organizacaoResponsavelId": "%s",
                          "projetoId": "%s",
                          "inicio": "2026-05-10T10:00:00Z",
                          "fim": "2026-05-10T11:00:00Z"
                        }
                        """.formatted(ORG_ID, projectId)))
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();

        String eventId = JsonPath.read(eventResponse, "$.id");

        // Recurrence rule with dataLimite within project end date should be allowed
        mockMvc.perform(put("/api/v1/eventos/{id}/recorrencia", eventId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "frequencia": "SEMANAL",
                            "intervalo": 1,
                            "diasDaSemana": ["SUNDAY"],
                            "dataLimite": "2026-05-30"
                        }
                        """))
                .andExpect(status().isOk());
    }
}
