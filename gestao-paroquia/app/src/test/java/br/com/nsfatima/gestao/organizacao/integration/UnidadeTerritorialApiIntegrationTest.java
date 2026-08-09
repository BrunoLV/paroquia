package br.com.nsfatima.gestao.organizacao.integration;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UnidadeTerritorialApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UnidadeTerritorialJpaRepository repository;

    private UUID matrizId;

    @BeforeEach
    void setUp() {
        matrizId = UUID.fromString("00000000-0000-0000-0000-000000000100");
        UnidadeTerritorialEntity matriz = repository.findById(matrizId).orElseGet(UnidadeTerritorialEntity::new);
        matriz.setId(matrizId);
        matriz.setNome("Matriz Nossa Senhora de Fátima Teste");
        matriz.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matriz.setAtivo(true);
        repository.save(matriz);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void list_deveRetornarUnidadesTerritoriais() throws Exception {
        mockMvc.perform(get("/api/v1/unidades-territoriais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_deveCriarNovaComunidade() throws Exception {
        String requestJson = """
                {
                    "nome": "Comunidade São José Teste API",
                    "tipo": "COMUNIDADE",
                    "contato": "contato@saojose.org",
                    "ativo": true,
                    "parentId": "%s"
                }
                """.formatted(matrizId);

        mockMvc.perform(post("/api/v1/unidades-territoriais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Comunidade São José Teste API"))
                .andExpect(jsonPath("$.tipo").value("COMUNIDADE"));
    }
}
