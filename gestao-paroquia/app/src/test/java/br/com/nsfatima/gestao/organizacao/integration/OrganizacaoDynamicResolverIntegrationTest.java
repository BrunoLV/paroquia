package br.com.nsfatima.gestao.organizacao.integration;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrganizacaoDynamicResolverIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrganizacaoJpaRepository organizacaoRepository;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Autowired
    private MembroOrganizacaoJpaRepository membershipRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldResolveCoordinatorAndViceCoordinatorNamesDynamically() throws Exception {
        // Create coordinator user
        UsuarioEntity coord = new UsuarioEntity();
        coord.setId(UUID.randomUUID());
        coord.setUsername("coord." + UUID.randomUUID() + "@example.com");
        coord.setPasswordHash("hash");
        coord.setNome("João da Silva Coordenador");
        coord.setEnabled(true);
        usuarioRepository.save(coord);

        // Create vice coordinator user
        UsuarioEntity vice = new UsuarioEntity();
        vice.setId(UUID.randomUUID());
        vice.setUsername("vice." + UUID.randomUUID() + "@example.com");
        vice.setPasswordHash("hash");
        vice.setNome("Maria de Souza Vice");
        vice.setEnabled(true);
        usuarioRepository.save(vice);

        // Create organization
        OrganizacaoEntity org = new OrganizacaoEntity();
        org.setId(UUID.randomUUID());
        org.setNome("Pastoral do Dynamic Test");
        org.setTipo(TipoOrganizacao.PASTORAL);
        org.setContato("test@test.com");
        org.setCoordenador("Fallback Coord Text"); // should be ignored in favor of João
        org.setViceCoordenador("Fallback Vice Text"); // should be ignored in favor of Maria
        org.setAtivo(true);
        organizacaoRepository.save(org);

        // Create memberships
        MembroOrganizacaoEntity m1 = new MembroOrganizacaoEntity();
        m1.setId(UUID.randomUUID());
        m1.setUsuarioId(coord.getId());
        m1.setOrganizacaoId(org.getId());
        m1.setTipoOrganizacao(TipoOrganizacao.PASTORAL.name());
        m1.setPapel(PapelOrganizacional.COORDENADOR.name());
        m1.setAtivo(true);
        membershipRepository.save(m1);

        MembroOrganizacaoEntity m2 = new MembroOrganizacaoEntity();
        m2.setId(UUID.randomUUID());
        m2.setUsuarioId(vice.getId());
        m2.setOrganizacaoId(org.getId());
        m2.setTipoOrganizacao(TipoOrganizacao.PASTORAL.name());
        m2.setPapel(PapelOrganizacional.VICE_COORDENADOR.name());
        m2.setAtivo(true);
        membershipRepository.save(m2);

        // Get organization and verify Resolved Names
        mockMvc.perform(get("/api/v1/organizacoes/" + org.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pastoral do Dynamic Test"))
                .andExpect(jsonPath("$.coordenador").value("João da Silva Coordenador"))
                .andExpect(jsonPath("$.viceCoordenador").value("Maria de Souza Vice"));
    }
}
