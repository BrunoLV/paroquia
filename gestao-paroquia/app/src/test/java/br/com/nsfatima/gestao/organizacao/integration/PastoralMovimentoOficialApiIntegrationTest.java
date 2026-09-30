package br.com.nsfatima.gestao.organizacao.integration;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class PastoralMovimentoOficialApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GrupoParoquialJpaRepository grupoParoquialRepository;

    @Autowired
    private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;

    private UUID conselhoId;

    @BeforeEach
    void setUp() {
        UUID cleroId = UUID.fromString("00000000-0000-0000-0000-0000000000dd");
        UUID matrizId = UUID.fromString("00000000-0000-0000-0000-000000000100");
        conselhoId = UUID.fromString("00000000-0000-0000-0000-0000000000cc");

        // Matriz (UnidadeTerritorial)
        UnidadeTerritorialEntity matriz = unidadeTerritorialRepository.findById(matrizId)
                .orElseGet(UnidadeTerritorialEntity::new);
        matriz.setId(matrizId);
        matriz.setNome("Igreja Matriz Teste");
        matriz.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matriz.setParentId(null);
        matriz.setAtivo(true);
        unidadeTerritorialRepository.save(matriz);

        // Conselho (GrupoParoquial)
        GrupoParoquialEntity conselho = grupoParoquialRepository.findById(conselhoId)
                .orElseGet(GrupoParoquialEntity::new);
        conselho.setId(conselhoId);
        conselho.setNome("Conselho Pastoral Teste");
        conselho.setTipo(TipoGrupo.CONSELHO);
        conselho.setParentId(matrizId);
        conselho.setAtivo(true);
        grupoParoquialRepository.save(conselho);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriarERecuperarOficialTipo() throws Exception {
        String payload = """
                {
                  "nome": "Pastoral de Teste Oficial",
                  "tipo": "PASTORAL"
                }
                """;

        String response = mockMvc.perform(post("/api/v1/pastorais-oficiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Pastoral de Teste Oficial"))
                .andExpect(jsonPath("$.tipo").value("PASTORAL"))
                .andReturn().getResponse().getContentAsString();

        // Extract ID
        String id = response.substring(response.indexOf("\"id\":\"") + 6, response.indexOf("\",\"nome\""));

        mockMvc.perform(get("/api/v1/pastorais-oficiais/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pastoral de Teste Oficial"))
                .andExpect(jsonPath("$.tipo").value("PASTORAL"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveBloquearOficialTipoCriacaoParaNaoAdmin() throws Exception {
        String payload = """
                {
                  "nome": "Pastoral Não Autorizada",
                  "tipo": "PASTORAL"
                }
                """;

        mockMvc.perform(post("/api/v1/pastorais-oficiais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveFalhaParaCriarGrupoParoquialTipoPastoralSemOficialVincular() throws Exception {
        String payload = String.format("""
                {
                  "nome": "Pastoral Sem Link",
                  "tipo": "PASTORAL",
                  "contato": "semlink@teste.com",
                  "coordenador": "Coordenador",
                  "viceCoordenador": "Vice",
                  "parentId": "%s",
                  "ativo": true
                }
                """, conselhoId);

        mockMvc.perform(post("/api/v1/organizacoes")
                        .with(user("paroco").roles("PAROCO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveCriarGrupoParoquialTipoPastoralComOficialVincular() throws Exception {
        // Create official type
        String officialPayload = """
                {
                  "nome": "Pastoral da Criança Oficial",
                  "tipo": "PASTORAL"
                }
                """;

        String officialResponse = mockMvc.perform(post("/api/v1/pastorais-oficiais")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(officialPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String officialId = officialResponse.substring(officialResponse.indexOf("\"id\":\"") + 6,
                officialResponse.indexOf("\",\"nome\""));


        String payload = String.format("""
                {
                  "nome": "Pastoral da Criança - Comunidade A",
                  "tipo": "PASTORAL",
                  "contato": "crianca@teste.com",
                  "coordenador": "Coordenador",
                  "viceCoordenador": "Vice",
                  "parentId": "%s",
                  "pastoralOficialId": "%s",
                  "ativo": true
                }
                """, conselhoId, officialId);

        mockMvc.perform(post("/api/v1/organizacoes")
                        .with(user("paroco").roles("PAROCO"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Pastoral da Criança - Comunidade A"))
                .andExpect(jsonPath("$.pastoralOficialId").value(officialId));
    }
}
