package br.com.nsfatima.gestao.membro.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.iam.infrastructure.security.ExternalMembershipReader;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import tools.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MembroControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MembroJpaRepository membroRepository;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Autowired
    private AuditoriaOperacaoJpaRepository auditoriaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final UUID ORG_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

    @BeforeEach
    void setUp() {
        membroRepository.deleteAll();
        auditoriaRepository.deleteAll();
    }

    private Authentication getAdminAuth() {
        UsuarioEntity adminUser = usuarioRepository.findByUsernameIgnoreCase("admin.test@paroquia.local")
                .orElseGet(() -> {
                    UsuarioEntity u = new UsuarioEntity();
                    u.setId(UUID.randomUUID());
                    u.setUsername("admin.test@paroquia.local");
                    u.setPasswordHash("pass");
                    u.setNome("Admin Teste");
                    u.setRoles("ROLE_ADMIN");
                    u.setEnabled(true);
                    return usuarioRepository.saveAndFlush(u);
                });

        UsuarioDetails details = new UsuarioDetails(
                adminUser.getId(), "admin", "pass", true, "ROLE_ADMIN",
                List.of(new ExternalMembershipReader.Membership(ORG_ID, "PASTORAL", "coordenador")));
        
        return UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
    }

    @Test
    @DisplayName("Deve criar um membro e registrar auditoria")
    void deveCriarMembroERegistrarAuditoria() throws Exception {
        var usuario = new UsuarioEntity();
        usuario.setId(UUID.randomUUID());
        usuario.setUsername("joao.silva@test.com");
        usuario.setPasswordHash("pass");
        usuario.setNome("João da Silva");
        usuario.setRoles("ROLE_USER");
        usuario.setEnabled(true);
        usuarioRepository.saveAndFlush(usuario);

        MembroRequest request = new MembroRequest(
                usuario.getId(),
                ORG_ID,
                "MEMBRO",
                LocalDate.now(),
                null,
                true
        );

        mockMvc.perform(post("/api/v1/membros")
                .with(authentication(getAdminAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioNome").value("João da Silva"));

        assertThat(membroRepository.findAll()).hasSize(1);
        assertThat(auditoriaRepository.findAll()).hasSize(1);
        assertThat(auditoriaRepository.findAll().getFirst().getRecursoTipo()).isEqualTo("MEMBRO");
        assertThat(auditoriaRepository.findAll().getFirst().getOrganizacaoId()).isEqualTo(ORG_ID);
    }

    @Test
    @DisplayName("Deve listar membros com filtros")
    void deveListarMembrosComFiltros() throws Exception {
        var userAlice = createUsuario("Alice");
        var userBob = createUsuario("Bob");

        createMembro(userAlice.getId(), ORG_ID);
        createMembro(userBob.getId(), ORG_ID);

        mockMvc.perform(get("/api/v1/membros")
                .with(authentication(getAdminAuth()))
                .param("organizacaoId", ORG_ID.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Deve adicionar participação em organização")
    void deveAdicionarParticipacao() throws Exception {
        var usuario = createUsuario("Membro Teste");

        var membroId = UUID.randomUUID();
        var entity = new MembroEntity();
        entity.setId(membroId);
        entity.setUsuarioId(usuario.getId());
        entity.setOrganizacaoId(ORG_ID);
        entity.setPapel("MEMBRO");
        entity.setAtivo(true);
        membroRepository.saveAndFlush(entity);

        ParticipacaoRequest request = new ParticipacaoRequest(ORG_ID, LocalDate.now());

        mockMvc.perform(post("/api/v1/membros/" + membroId + "/organizacoes")
                .with(authentication(getAdminAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organizacaoId").value(ORG_ID.toString()));
    }

    @Test
    @DisplayName("Deve negar acesso para usuário sem role ADMIN")
    @WithMockUser(roles = "USER")
    void deveNegarAcessoSemRoleAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/membros"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    private UsuarioEntity createUsuario(String nome) {
        var usuario = new UsuarioEntity();
        usuario.setId(UUID.randomUUID());
        usuario.setUsername(nome.toLowerCase().replace(" ", ".") + "@test.com");
        usuario.setPasswordHash("pass");
        usuario.setNome(nome);
        usuario.setRoles("ROLE_USER");
        usuario.setEnabled(true);
        return usuarioRepository.saveAndFlush(usuario);
    }

    private void createMembro(UUID usuarioId, UUID orgId) {
        var entity = new MembroEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsuarioId(usuarioId);
        entity.setOrganizacaoId(orgId);
        entity.setPapel("MEMBRO");
        entity.setAtivo(true);
        membroRepository.saveAndFlush(entity);
    }
}
