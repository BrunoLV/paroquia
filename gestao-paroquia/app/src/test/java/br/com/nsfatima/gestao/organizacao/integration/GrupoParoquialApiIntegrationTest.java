package br.com.nsfatima.gestao.organizacao.integration;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.PastoralMovimentoOficialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.PastoralMovimentoOficialJpaRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class GrupoParoquialApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GrupoParoquialJpaRepository grupoParoquialRepository;

    @Autowired
    private PastoralMovimentoOficialJpaRepository pastoralOficialRepository;

    @Autowired
    private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;

    private UUID conselhoId;
    private UUID pastoralOficialId;

    @BeforeEach
    void setUp() {
        // Usar UUIDs estáticos compatíveis com o seed e atualizar seus tipos e parentIds
        UUID matrizId = UUID.fromString("00000000-0000-0000-0000-000000000100");
        UUID conselhoIdVal = UUID.fromString("00000000-0000-0000-0000-0000000000cc");

        // 0. Official catalog
        pastoralOficialId = UUID.fromString("50000000-0000-0000-0000-000000000014");
        PastoralMovimentoOficialEntity oficial =
                pastoralOficialRepository.findById(pastoralOficialId).orElseGet(PastoralMovimentoOficialEntity::new);
        oficial.setId(pastoralOficialId);
        oficial.setNome("Pastoral da Comunicação Oficial");
        oficial.setTipo(TipoGrupo.PASTORAL);
        pastoralOficialRepository.save(oficial);

        // 1. Matriz (UnidadeTerritorial)
        UnidadeTerritorialEntity matriz = unidadeTerritorialRepository.findById(matrizId)
                .orElseGet(UnidadeTerritorialEntity::new);
        matriz.setId(matrizId);
        matriz.setNome("Igreja Matriz");
        matriz.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matriz.setParentId(null);
        matriz.setAtivo(true);
        unidadeTerritorialRepository.save(matriz);

        // 3. Conselho (GrupoParoquial)
        GrupoParoquialEntity conselho = grupoParoquialRepository.findById(conselhoIdVal)
                .orElseGet(GrupoParoquialEntity::new);
        conselho.setId(conselhoIdVal);
        conselho.setNome("Conselho Pastoral");
        conselho.setTipo(TipoGrupo.CONSELHO);
        conselho.setParentId(matrizId);
        conselho.setAtivo(true);
        grupoParoquialRepository.save(conselho);

        conselhoId = conselhoIdVal;
    }

    @Test
    @WithMockUser(roles = "PAROCO")
    void deveCriarERecuperarGrupoParoquial() throws Exception {
        String payload = String.format("""
                {
                  "nome": "Pastoral da Música",
                  "tipo": "PASTORAL",
                  "contato": "musica@nsfatima.com",
                  "coordenador": "Pedro Coordenador",
                  "viceCoordenador": "Ana Vice",
                  "parentId": "%s",
                  "pastoralOficialId": "%s",
                  "ativo": true
                }
                """, conselhoId, pastoralOficialId);

        String response = mockMvc.perform(post("/api/v1/organizacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Pastoral da Música"))
                .andExpect(jsonPath("$.coordenador").value("Pedro Coordenador"))
                .andExpect(jsonPath("$.viceCoordenador").value("Ana Vice"))
                .andReturn().getResponse().getContentAsString();

        // Simple way to get ID from response
        String id = response.substring(response.indexOf("\"id\":\"") + 6, response.indexOf("\",\"nome\""));

        mockMvc.perform(get("/api/v1/organizacoes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pastoral da Música"))
                .andExpect(jsonPath("$.tipo").value("PASTORAL"))
                .andExpect(jsonPath("$.coordenador").value("Pedro Coordenador"))
                .andExpect(jsonPath("$.viceCoordenador").value("Ana Vice"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveNegarCriarParaAdminTecnico() throws Exception {
        String payload = String.format("""
                {
                  "nome": "Pastoral da Acolhida",
                  "tipo": "PASTORAL",
                  "contato": "acolhida@nsfatima.com",
                  "coordenador": "Carlos Coordenador",
                  "viceCoordenador": "Lucas Vice",
                  "parentId": "%s",
                  "pastoralOficialId": "%s",
                  "ativo": true
                }
                """, conselhoId, pastoralOficialId);

        mockMvc.perform(post("/api/v1/organizacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarCriarParaNaoParoco() throws Exception {
        String payload = String.format("""
                {
                  "nome": "Tentativa",
                  "tipo": "PASTORAL",
                  "pastoralOficialId": "%s",
                  "ativo": true
                }
                """, pastoralOficialId);

        mockMvc.perform(post("/api/v1/organizacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveListarGrupoParoquialMembros() throws Exception {
        // Create a mock UUID
        UUID orgId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/organizacoes/" + orgId + "/membros")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveRecuperarOrganogramaArvore() throws Exception {
        mockMvc.perform(get("/api/v1/organizacoes/organograma")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").isNotEmpty())
                .andExpect(jsonPath("$.subtype").value("GOVERNO_ECLESIASTICO"))
                .andExpect(jsonPath("$.children").isArray());
    }
}
