package br.com.nsfatima.gestao.calendario.contract;

import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class ProjetosContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoEventoJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        ProjetoEventoEntity entity = new ProjetoEventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setNome("Projeto Pastoral");
        entity.setDescricao("Planejamento");
        entity.setGrupoParoquialResponsavelId(UUID.randomUUID());
        entity.setInicioUtc(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-12-31T23:59:59Z"));
        repository.save(entity);
    }

    @Test
    void deveListarProjetos() throws Exception {
        mockMvc.perform(get("/api/v1/projetos")
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isString())
                .andExpect(jsonPath("$[0].nome").value("Projeto Pastoral"))
                .andExpect(jsonPath("$[0].grupoParoquialResponsavelId").isString())
                .andExpect(jsonPath("$[0].inicio").isString())
                .andExpect(jsonPath("$[0].fim").isString())
                .andExpect(jsonPath("$[0].status").value("ABERTO"))
                .andExpect(jsonPath("$[0].updated").value(false));
    }

    @Test
    void deveCriarProjeto() throws Exception {
        String orgId = UUID.randomUUID().toString();
        mockMvc.perform(post("/api/v1/projetos")
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("""
                                {
                                  "nome": "Semana Santa",
                                  "descricao": "Mutirao liturgico",
                                  "grupoParoquialResponsavelId": "%s",
                                  "inicio": "2026-03-20T10:00:00Z",
                                  "fim": "2026-03-27T22:00:00Z"
                                }
                                """.formatted(orgId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Semana Santa"))
                .andExpect(jsonPath("$.grupoParoquialResponsavelId").value(orgId))
                .andExpect(jsonPath("$.status").value("ABERTO"));
    }
}
