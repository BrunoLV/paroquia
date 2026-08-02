package br.com.nsfatima.gestao.calendario.integration.projetos;

import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class ProjetoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoEventoJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should perform full project lifecycle")
    void deveRealizarCompletoCicloVida() throws Exception {
        String orgId = UUID.randomUUID().toString();

        // Create
        String createPayload = """
                {
                  "nome": "Projeto Lifecycle",
                  "descricao": "Teste de ciclo de vida",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-10-01T00:00:00Z",
                  "fim": "2026-10-31T23:59:59Z"
                }
                """.formatted(orgId);

        String response = mockMvc.perform(post("/api/v1/projetos")
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Projeto Lifecycle"))
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andReturn().getResponse().getContentAsString();

        String projectId = JsonPath.read(response, "$.id");

        // List
        mockMvc.perform(get("/api/v1/projetos")
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .header("X-Actor-Org-Id", orgId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nome == 'Projeto Lifecycle')]").exists());

        // Patch
        String patchPayload = """
                {
                  "nome": "Projeto Lifecycle Atualizado",
                  "status": "CONCLUIDO"
                }
                """;

        mockMvc.perform(patch("/api/v1/projetos/{id}", projectId)
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .header("X-Actor-Org-Id", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Projeto Lifecycle Atualizado"))
                .andExpect(jsonPath("$.status").value("CONCLUIDO"))
                .andExpect(jsonPath("$.updated").value(true));
    }

    @Test
    @DisplayName("Should fail when project name is too long")
    void deveFalhaParaLongoNome() throws Exception {
        String longName = "A".repeat(161);
        String payload = """
                {
                  "nome": "%s",
                  "organizacaoResponsavelId": "%s",
                  "inicio": "2026-01-01T00:00:00Z",
                  "fim": "2026-01-02T00:00:00Z"
                }
                """.formatted(longName, UUID.randomUUID());

        mockMvc.perform(post("/api/v1/projetos")
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .header("X-Actor-Org-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent project")
    void deveRetornar404() throws Exception {
        String patchPayload = """
                {
                  "nome": "Inexistente",
                  "status": "CONCLUIDO"
                }
                """;

        mockMvc.perform(patch("/api/v1/projetos/{id}", UUID.randomUUID())
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CPC")
                        .header("X-Actor-Org-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchPayload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }
}
