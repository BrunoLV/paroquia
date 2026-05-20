package br.com.nsfatima.gestao.calendario.contract;

import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjetoMutacaoContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoEventoJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        ProjetoEventoEntity entity = new ProjetoEventoEntity();
        entity.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        entity.setNome("Projeto Original");
        entity.setDescricao("Descricao original");
        entity.setOrganizacaoResponsavelId(UUID.randomUUID());
        entity.setInicioUtc(java.time.Instant.parse("2026-01-01T00:00:00Z"));
        entity.setFimUtc(java.time.Instant.parse("2026-12-31T23:59:59Z"));
        repository.save(entity);
    }

    @Test
    void shouldPatchProjeto() throws Exception {
        mockMvc.perform(patch("/api/v1/projetos/{projetoId}", "00000000-0000-0000-0000-000000000001")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"nome\":\"Projeto Atualizado\",\"descricao\":\"Planejamento revisado\",\"status\":\"ATIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("00000000-0000-0000-0000-000000000001"))
                .andExpect(jsonPath("$.nome").value("Projeto Atualizado"))
                .andExpect(jsonPath("$.descricao").value("Planejamento revisado"))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.updated").value(true));
    }
}
