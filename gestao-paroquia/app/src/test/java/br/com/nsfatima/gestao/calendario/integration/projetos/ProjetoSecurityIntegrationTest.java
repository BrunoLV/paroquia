package br.com.nsfatima.gestao.calendario.integration.projetos;

import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjetoSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoEventoJpaRepository projetoRepository;

    private final UUID orgId = UUID.randomUUID();
    private final UUID otherOrgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        projetoRepository.deleteAll();
    }

    @Test
    void shouldAllowCoordenadorToCreateProjectForTheirOrg() throws Exception {
        mockMvc.perform(post("/api/v1/projetos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", orgId.toString())
                .header("X-Actor-User-Id", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Meu Projeto",
                          "organizacaoResponsavelId": "%s",
                          "inicio": "2026-01-01T00:00:00Z",
                          "fim": "2026-01-31T23:59:59Z"
                        }
                        """.formatted(orgId)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldDenyCoordenadorToCreateProjectForOtherOrg() throws Exception {
        mockMvc.perform(post("/api/v1/projetos")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", orgId.toString())
                .header("X-Actor-User-Id", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Projeto Alheio",
                          "organizacaoResponsavelId": "%s",
                          "inicio": "2026-01-01T00:00:00Z",
                          "fim": "2026-01-31T23:59:59Z"
                        }
                        """.formatted(otherOrgId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowParocoToCreateProjectForAnyOrg() throws Exception {
        mockMvc.perform(post("/api/v1/projetos")
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .header("X-Actor-Org-Id", UUID.randomUUID().toString())
                .header("X-Actor-User-Id", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "nome": "Projeto Global",
                          "organizacaoResponsavelId": "%s",
                          "inicio": "2026-01-01T00:00:00Z",
                          "fim": "2026-01-31T23:59:59Z"
                        }
                        """.formatted(otherOrgId)))
                .andExpect(status().isCreated());
    }
}
