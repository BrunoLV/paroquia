package br.com.nsfatima.gestao.iam.integration;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
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
    private MembroOrganizacaoJpaRepository membershipRepository;

    @Autowired
    private OrganizacaoJpaRepository organizacaoRepository;

    private UUID coordinatorId;
    private UUID memberId;
    private UUID strangerId;
    private UUID orgId;
    private UUID otherOrgId;
    private UUID churchId;

    @BeforeEach
    void setUp() {
        churchId = UUID.randomUUID();
        var church = new OrganizacaoEntity();
        church.setId(churchId);
        church.setNome("Igreja Matriz Teste");
        church.setTipo(TipoOrganizacao.MATRIZ);
        church.setAtivo(true);
        organizacaoRepository.save(church);

        orgId = UUID.randomUUID();
        var org = new OrganizacaoEntity();
        org.setId(orgId);
        org.setNome("Pastoral Teste");
        org.setTipo(TipoOrganizacao.PASTORAL);
        org.setAtivo(true);
        organizacaoRepository.save(org);

        otherOrgId = UUID.randomUUID();
        var otherOrg = new OrganizacaoEntity();
        otherOrg.setId(otherOrgId);
        otherOrg.setNome("Outra Pastoral");
        otherOrg.setTipo(TipoOrganizacao.PASTORAL);
        otherOrg.setAtivo(true);
        organizacaoRepository.save(otherOrg);

        coordinatorId = createUser("coordinator");
        memberId = createUser("member");
        strangerId = createUser("stranger");

        createMembership(coordinatorId, orgId, "coordenador-pastoral-local");
        createMembership(memberId, orgId, "voluntario-pastoral-local");
        createMembership(strangerId, otherOrgId, "voluntario-pastoral-local");
    }

    private UUID createUser(String username) {
        UsuarioEntity user = new UsuarioEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setEnabled(true);
        return usuarioRepository.save(user).getId();
    }

    private void createMembership(UUID userId, UUID organizationId, String papel) {
        MembroOrganizacaoEntity m = new MembroOrganizacaoEntity();
        m.setId(UUID.randomUUID());
        m.setUsuarioId(userId);
        m.setOrganizacaoId(organizationId);
        m.setTipoOrganizacao("PASTORAL");
        m.setPapel(papel);
        m.setAtivo(true);
        membershipRepository.save(m);
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

        mockMvc.perform(post("/api/v1/usuarios/" + memberId + "/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\": \"new-pass\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void coordinatorShouldNotResetPasswordOfStranger() throws Exception {
        authenticateAs(coordinatorId, "ROLE_COORDENADOR");

        mockMvc.perform(post("/api/v1/usuarios/" + strangerId + "/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\": \"new-pass\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldResetAnyPassword() throws Exception {
        authenticateAs(coordinatorId, "ROLE_ADMIN"); // coordinator acting as admin

        mockMvc.perform(post("/api/v1/usuarios/" + strangerId + "/password-reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\": \"new-pass\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void coordinatorShouldAddMembershipToTheirOrg() throws Exception {
        authenticateAs(coordinatorId, "ROLE_COORDENADOR");

        UUID newUser = createUser("new_user");

        mockMvc.perform(post("/api/v1/usuarios/" + newUser + "/membros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizacaoId\": \"" + orgId
                                + "\", \"tipo\": \"PASTORAL\", \"papel\": \"VOLUNTARIO_PASTORAL_LOCAL\", \"igrejaVinculoId\": \""
                                + churchId + "\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void coordinatorShouldNotAddMembershipToOtherOrg() throws Exception {
        authenticateAs(coordinatorId, "ROLE_COORDENADOR");

        UUID newUser = createUser("new_user");

        mockMvc.perform(post("/api/v1/usuarios/" + newUser + "/membros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizacaoId\": \"" + otherOrgId + "\", \"tipo\": \"PASTORAL\", \"papel\": " +
                                "\"VOLUNTARIO_PASTORAL_LOCAL\", \"igrejaVinculoId\": \"" + churchId + "\"}"))
                .andExpect(status().isForbidden());
    }
}
