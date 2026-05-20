package br.com.nsfatima.gestao.calendario.integration.projetos;

import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VinculoProjetoEventoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoEventoJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldSupportProjectLinkingLifecycle() throws Exception {
        String orgId = UUID.randomUUID().toString();
        
        mockMvc.perform(post("/api/v1/projetos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", orgId)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                          "nome": "Projeto Vinculado",
                          "descricao": "Fluxo tipado",
                          "organizacaoResponsavelId": "%s",
                          "inicio": "2026-01-01T00:00:00Z",
                          "fim": "2026-12-31T23:59:59Z"
                        }
                        """.formatted(orgId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Projeto Vinculado"));

        mockMvc.perform(get("/api/v1/projetos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Projeto Vinculado"));
    }
}
