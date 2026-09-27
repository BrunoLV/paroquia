package br.com.nsfatima.gestao.organizacao.integration;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.support.FielTestFixtures;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GrupoParoquialDynamicResolverIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GrupoParoquialJpaRepository grupoParoquialRepository;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Autowired
    private FielJpaRepository fielRepository;

    @Autowired
    private MembroGrupoJpaRepository vinculoRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveResolverCoordenadorEViceCoordenadorNomesDynamically() throws Exception {
        // Create coordinator user
        UsuarioEntity coord = new UsuarioEntity();
        coord.setId(UUID.randomUUID());
        coord.setUsername("coord." + UUID.randomUUID() + "@example.com");
        coord.setPasswordHash("hash");
        coord.setEnabled(true);
        coord.setFielId(FielTestFixtures.criarFiel(fielRepository, "João da Silva Coordenador"));
        usuarioRepository.save(coord);

        // Create vice coordinator user
        UsuarioEntity vice = new UsuarioEntity();
        vice.setId(UUID.randomUUID());
        vice.setUsername("vice." + UUID.randomUUID() + "@example.com");
        vice.setPasswordHash("hash");
        vice.setEnabled(true);
        vice.setFielId(FielTestFixtures.criarFiel(fielRepository, "Maria de Souza Vice"));
        usuarioRepository.save(vice);

        // Create organization
        GrupoParoquialEntity org = new GrupoParoquialEntity();
        org.setId(UUID.randomUUID());
        org.setNome("Pastoral do Dynamic Test");
        org.setTipo(TipoGrupo.PASTORAL);
        org.setContato("test@test.com");
        org.setCoordenador("Fallback Coord Text"); // should be ignored in favor of João
        org.setViceCoordenador("Fallback Vice Text"); // should be ignored in favor of Maria
        org.setAtivo(true);
        grupoParoquialRepository.save(org);

        // Create vinculos
        MembroGrupoEntity m1 = new MembroGrupoEntity();
        m1.setId(UUID.randomUUID());
        m1.setFielId(coord.getFielId());
        m1.setGrupoParoquialId(org.getId());
        m1.setTipoGrupo(TipoGrupo.PASTORAL);
        m1.setPapel(PapelOrganizacional.COORDENADOR.storedValue());
        m1.setAtivo(true);
        vinculoRepository.save(m1);

        MembroGrupoEntity m2 = new MembroGrupoEntity();
        m2.setId(UUID.randomUUID());
        m2.setFielId(vice.getFielId());
        m2.setGrupoParoquialId(org.getId());
        m2.setTipoGrupo(TipoGrupo.PASTORAL);
        m2.setPapel("vice-coordenador");
        m2.setAtivo(true);
        vinculoRepository.save(m2);

        // Get organization and verify Resolved Names
        mockMvc.perform(get("/api/v1/organizacoes/" + org.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pastoral do Dynamic Test"))
                .andExpect(jsonPath("$.coordenador").value("João da Silva Coordenador"))
                .andExpect(jsonPath("$.viceCoordenador").value("Maria de Souza Vice"));
    }
}
