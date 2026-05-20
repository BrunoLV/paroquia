package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class EventoProjetoLinkIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoEventoJpaRepository projetoRepository;

    @Autowired
    private EventoJpaRepository eventoRepository;

    private UUID projetoId;
    private static final String ORG_ID = "00000000-0000-0000-0000-0000000000aa";

    @BeforeEach
    void setUp() {
        eventoRepository.deleteAll();
        projetoRepository.deleteAll();
        ProjetoEventoEntity projeto = new ProjetoEventoEntity();
        projeto.setId(UUID.randomUUID());
        projeto.setNome("Projeto Teste");
        projeto.setOrganizacaoResponsavelId(UUID.fromString(ORG_ID));
        projeto.setInicioUtc(Instant.parse("2026-01-01T00:00:00Z"));
        projeto.setFimUtc(Instant.parse("2026-01-31T23:59:59Z"));
        projeto.setStatus("ATIVO");
        projetoId = projetoRepository.save(projeto).getId();
    }

    @Test
    void shouldLinkEventToProjectWithinTimeframe() throws Exception {
        mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento do Projeto",
                          "organizacaoResponsavelId": "%s",
                          "projetoId": "%s",
                          "inicio": "2026-01-10T10:00:00Z",
                          "fim": "2026-01-10T12:00:00Z"
                        }
                        """.formatted(ORG_ID, projetoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projetoId").value(projetoId.toString()))
                .andExpect(jsonPath("$.nomeProjeto").value("Projeto Teste"));
    }

    @Test
    void shouldFailWhenEventIsOutsideProjectTimeframe() throws Exception {
        mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento Fora do Prazo",
                          "organizacaoResponsavelId": "%s",
                          "projetoId": "%s",
                          "inicio": "2026-02-01T10:00:00Z",
                          "fim": "2026-02-01T12:00:00Z"
                        }
                        """.formatted(ORG_ID, projetoId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenProjectIsInactive() throws Exception {
        ProjetoEventoEntity projeto = projetoRepository.findById(projetoId).get();
        projeto.setStatus("INATIVO");
        projetoRepository.save(projeto);

        mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento em Projeto Inativo",
                          "organizacaoResponsavelId": "%s",
                          "projetoId": "%s",
                          "inicio": "2026-01-10T10:00:00Z",
                          "fim": "2026-01-10T12:00:00Z"
                        }
                        """.formatted(ORG_ID, projetoId)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldFilterEventsByProject() throws Exception {
        // 1. Create event linked to project
        mockMvc.perform(post("/api/v1/eventos")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "titulo": "Evento do Projeto",
                          "organizacaoResponsavelId": "%s",
                          "projetoId": "%s",
                          "inicio": "2026-01-10T10:00:00Z",
                          "fim": "2026-01-10T12:00:00Z"
                        }
                        """.formatted(ORG_ID, projetoId)))
                .andExpect(status().isCreated());

        // 2. List with project filter
        mockMvc.perform(get("/api/v1/eventos")
                .param("projetoId", projetoId.toString())
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Evento do Projeto"))
                .andExpect(jsonPath("$.content[0].nomeProjeto").value("Projeto Teste"));
    }
}
