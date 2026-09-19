package br.com.nsfatima.gestao.iam.integration;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.support.FielTestFixtures;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioDelegatedAdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Autowired
    private FielJpaRepository fielRepository;

    @Autowired
    private MembroGrupoJpaRepository vinculoRepository;

    @Autowired
    private GrupoParoquialJpaRepository grupoParoquialRepository;

    @Autowired
    private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;

    private UUID coordinatorId;
    private UUID memberId;
    private UUID strangerId;
    private UUID orgId;
    private UUID otherOrgId;
    private UUID churchId;

    @BeforeEach
    void setUp() {
        churchId = UUID.randomUUID();
        var church = new UnidadeTerritorialEntity();
        church.setId(churchId);
        church.setNome("Igreja Matriz Teste");
        church.setTipo(TipoUnidadeTerritorial.MATRIZ);
        church.setAtivo(true);
        unidadeTerritorialRepository.save(church);

        orgId = UUID.randomUUID();
        var org = new GrupoParoquialEntity();
        org.setId(orgId);
        org.setNome("Pastoral Teste");
        org.setTipo(TipoGrupo.PASTORAL);
        org.setAtivo(true);
        grupoParoquialRepository.save(org);

        otherOrgId = UUID.randomUUID();
        var otherOrg = new GrupoParoquialEntity();
        otherOrg.setId(otherOrgId);
        otherOrg.setNome("Outra Pastoral");
        otherOrg.setTipo(TipoGrupo.PASTORAL);
        otherOrg.setAtivo(true);
        grupoParoquialRepository.save(otherOrg);

        coordinatorId = createUser("coordinator");
        memberId = createUser("member");
        strangerId = createUser("stranger");

        createVinculo(coordinatorId, orgId, "coordenador-pastoral-local");
        createVinculo(memberId, orgId, "voluntario-pastoral-local");
        createVinculo(strangerId, otherOrgId, "voluntario-pastoral-local");
    }

    private UUID createUser(String username) {
        UsuarioEntity user = new UsuarioEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setEnabled(true);
        user.setFielId(FielTestFixtures.criarFiel(fielRepository));
        return usuarioRepository.save(user).getId();
    }

    private void createVinculo(UUID userId, UUID organizationId, String papel) {
        UUID fielId = usuarioRepository.findById(userId).orElseThrow().getFielId();
        MembroGrupoEntity m = new MembroGrupoEntity();
        m.setId(UUID.randomUUID());
        m.setFielId(fielId);
        m.setGrupoParoquialId(organizationId);
        m.setTipoGrupo(TipoGrupo.PASTORAL);
        m.setPapel(papel);
        m.setAtivo(true);
        vinculoRepository.save(m);
    }

    private void authenticateAs(UUID userId, String roles) {
        UsuarioEntity user = usuarioRepository.findById(userId).orElseThrow();
        UsuarioDetails details = new UsuarioDetails(
                user.getId(), user.getUsername(), user.getPasswordHash(), user.isEnabled(), roles, List.of());

        // Note: In integration tests with MockMvc, we usually use @WithMockUser or manually set context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities())
        );
    }

    @Test
    void coordinatorShouldResetPasswordOfMemberInSameOrg() throws Exception {
        authenticateAs(coordinatorId, "ROLE_COORDENADOR");

        mockMvc.perform(post("/api/v1/usuarios/" + memberId + "/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\": \"new-pass\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void coordinatorShouldNotResetPasswordOfStranger() throws Exception {
        authenticateAs(coordinatorId, "ROLE_COORDENADOR");

        mockMvc.perform(post("/api/v1/usuarios/" + strangerId + "/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\": \"new-pass\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldResetAnyPassword() throws Exception {
        authenticateAs(coordinatorId, "ROLE_ADMIN"); // coordinator acting as admin

        mockMvc.perform(post("/api/v1/usuarios/" + strangerId + "/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\": \"new-pass\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void coordinatorShouldAddVinculoToTheirOrg() throws Exception {
        authenticateAs(coordinatorId, "ROLE_COORDENADOR");

        UUID newUser = createUser("new_user");

        mockMvc.perform(post("/api/v1/usuarios/" + newUser + "/membros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grupoParoquialId\": \"" + orgId
                                + "\", \"tipo\": \"PASTORAL\", \"papel\": \"VOLUNTARIO_PASTORAL_LOCAL\""
                                + ", \"igrejaVinculoId\": \"" + churchId + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void coordinatorShouldNotAddVinculoToOtherOrg() throws Exception {
        authenticateAs(coordinatorId, "ROLE_COORDENADOR");

        UUID newUser = createUser("new_user");

        mockMvc.perform(post("/api/v1/usuarios/" + newUser + "/membros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"grupoParoquialId\": \"" + otherOrgId + "\", \"tipo\": \"PASTORAL\", \"papel\": " +
                                "\"VOLUNTARIO_PASTORAL_LOCAL\", \"igrejaVinculoId\": \"" + churchId + "\"}"))
                .andExpect(status().isForbidden());
    }
}
