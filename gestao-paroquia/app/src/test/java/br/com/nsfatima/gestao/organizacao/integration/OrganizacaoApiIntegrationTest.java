package br.com.nsfatima.gestao.organizacao.integration;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.PastoralMovimentoOficialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.PastoralMovimentoOficialJpaRepository;
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
class OrganizacaoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizacaoJpaRepository organizacaoRepository;

    @Autowired
    private PastoralMovimentoOficialJpaRepository pastoralOficialRepository;

    private UUID conselhoId;
    private UUID pastoralOficialId;

    @BeforeEach
    void setUp() {
        // Usar UUIDs estáticos compatíveis com o seed e atualizar seus tipos e parentIds
        UUID cleroId = UUID.fromString("00000000-0000-0000-0000-0000000000dd");
        UUID matrizId = UUID.fromString("00000000-0000-0000-0000-000000000100");
        UUID conselhoIdVal = UUID.fromString("00000000-0000-0000-0000-0000000000cc");

        // 0. Official catalog
        pastoralOficialId = UUID.fromString("50000000-0000-0000-0000-000000000014");
        PastoralMovimentoOficialEntity oficial =
                pastoralOficialRepository.findById(pastoralOficialId).orElseGet(PastoralMovimentoOficialEntity::new);
        oficial.setId(pastoralOficialId);
        oficial.setNome("Pastoral da Comunicação Oficial");
        oficial.setTipo(TipoOrganizacao.PASTORAL);
        pastoralOficialRepository.save(oficial);

        // 1. Clero
        OrganizacaoEntity clero = organizacaoRepository.findById(cleroId).orElseGet(OrganizacaoEntity::new);
        clero.setId(cleroId);
        clero.setNome("Clero Paroquial");
        clero.setTipo(TipoOrganizacao.CLERO);
        clero.setAtivo(true);
        clero.setParentId(null);
        organizacaoRepository.save(clero);

        // 2. Matriz
        OrganizacaoEntity matriz = organizacaoRepository.findById(matrizId).orElseGet(OrganizacaoEntity::new);
        matriz.setId(matrizId);
        matriz.setNome("Igreja Matriz");
        matriz.setTipo(TipoOrganizacao.MATRIZ);
        matriz.setParentId(cleroId);
        matriz.setAtivo(true);
        organizacaoRepository.save(matriz);

        // 3. Conselho
        OrganizacaoEntity conselho = organizacaoRepository.findById(conselhoIdVal).orElseGet(OrganizacaoEntity::new);
        conselho.setId(conselhoIdVal);
        conselho.setNome("Conselho Pastoral");
        conselho.setTipo(TipoOrganizacao.CONSELHO_PASTORAL);
        conselho.setParentId(matrizId);
        conselho.setAtivo(true);
        organizacaoRepository.save(conselho);

        conselhoId = conselhoIdVal;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateAndRetrieveOrganization() throws Exception {
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
    @WithMockUser(roles = "CONSELHO_COORDENADOR")
    void shouldAllowCreateForConselhoCoordenador() throws Exception {
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Pastoral da Acolhida"))
                .andExpect(jsonPath("$.coordenador").value("Carlos Coordenador"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyCreateForNonAdmin() throws Exception {
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
    void shouldListOrganizationMembers() throws Exception {
        // Create a mock UUID
        UUID orgId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/organizacoes/" + orgId + "/membros")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRetrieveOrganogramaTree() throws Exception {
        mockMvc.perform(get("/api/v1/organizacoes/organograma")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.label").value("Clero Paroquial"))
                .andExpect(jsonPath("$.subtype").value("CLERO"))
                .andExpect(jsonPath("$.children").isArray());
    }
}
