package br.com.nsfatima.gestao.membro.integration;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.support.FielTestFixtures;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.IdoneidadeRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorRequest;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MembroControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MembroGrupoJpaRepository membroRepository;
    @Autowired
    private UsuarioJpaRepository usuarioRepository;
    @Autowired
    private FielJpaRepository fielRepository;
    @Autowired
    private OrganizacaoJpaRepository organizacaoRepository;
    @Autowired
    private AuditoriaOperacaoJpaRepository auditoriaRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private UUID orgId;

    @BeforeEach
    void setUp() {
        membroRepository.deleteAll();
        auditoriaRepository.deleteAll();

        OrganizacaoEntity org = new OrganizacaoEntity();
        org.setId(UUID.randomUUID());
        org.setNome("Pastoral de Teste " + UUID.randomUUID());
        org.setTipo(TipoOrganizacao.PASTORAL);
        org.setAtivo(true);
        organizacaoRepository.saveAndFlush(org);
        orgId = org.getId();
    }

    private Authentication getParocoAuth() {
        UsuarioEntity parocoUser = usuarioRepository.findByUsernameIgnoreCase("paroco.test@paroquia.local")
                .orElseGet(() -> {
                    UsuarioEntity u = new UsuarioEntity();
                    u.setId(UUID.randomUUID());
                    u.setUsername("paroco.test@paroquia.local");
                    u.setPasswordHash("pass");
                    u.setRoles("ROLE_CLERO_PAROCO,ROLE_GLOBAL_PAROCO");
                    u.setEnabled(true);
                    u.setFielId(FielTestFixtures.criarFiel(fielRepository, "Pároco Teste"));
                    return usuarioRepository.saveAndFlush(u);
                });

        UsuarioDetails details = new UsuarioDetails(
                parocoUser.getId(), "paroco", "pass", true, "ROLE_CLERO_PAROCO,ROLE_GLOBAL_PAROCO",
                List.of(new LeitorVinculoExterno.Vinculo(orgId, "CLERO", "paroco")));

        return UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
    }

    @Test
    @DisplayName("Deve criar um membro e registrar auditoria")
    void deveCriarMembroERegistrarAuditoria() throws Exception {
        UUID fielId = FielTestFixtures.criarFiel(fielRepository, "João da Silva");

        MembroRequest request = new MembroRequest(
                fielId,
                orgId,
                "MEMBRO_ATIVO",
                LocalDate.now(),
                null,
                true
        );

        mockMvc.perform(post("/api/v1/membros")
                        .with(authentication(getParocoAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fielNome").value("João da Silva"));

        assertThat(membroRepository.findAll()).hasSize(1);
        assertThat(auditoriaRepository.findAll()).hasSize(1);
        assertThat(auditoriaRepository.findAll().getFirst().getRecursoTipo()).isEqualTo("MEMBRO");
        assertThat(auditoriaRepository.findAll().getFirst().getOrganizacaoId()).isEqualTo(orgId);
    }

    @Test
    @DisplayName("Deve listar membros com filtros")
    void deveListarMembrosComFiltros() throws Exception {
        createMembro(FielTestFixtures.criarFiel(fielRepository, "Alice"));
        createMembro(FielTestFixtures.criarFiel(fielRepository, "Bob"));

        mockMvc.perform(get("/api/v1/membros")
                        .with(authentication(getParocoAuth()))
                        .param("organizacaoId", orgId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Deve negar acesso para usuário sem role autorizada")
    @WithMockUser(roles = "USER")
    void deveNegarAcessoSemRoleAutorizada() throws Exception {
        mockMvc.perform(get("/api/v1/membros"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve promover um Fiel membro a coordenador criando um novo Usuario mediante provisão do Pároco")
    void devePromoverFielACoordenadorCriandoUsuario() throws Exception {
        UUID fielId = FielTestFixtures.criarFiel(fielRepository, "Carlos Promovido");
        UUID membroId = createMembro(fielId);
        atestarIdoneidade(membroId);

        var request = new PromoverACoordenadorRequest(
                fielId, orgId, "coordenador", "carlos.promovido", "senhaForte123");

        mockMvc.perform(post("/api/v1/membros/promover-coordenador")
                        .with(authentication(getParocoAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioCriado").value(true))
                .andExpect(jsonPath("$.fielId").value(fielId.toString()));
    }

    @Test
    @DisplayName("Deve recusar a promoção de um Fiel sem idoneidade pastoral registrada")
    void deveRecusarPromocaoDeFielSemIdoneidade() throws Exception {
        UUID fielId = FielTestFixtures.criarFiel(fielRepository, "Fiel Sem Idoneidade");
        createMembro(fielId);

        var request = new PromoverACoordenadorRequest(
                fielId, orgId, "coordenador", "fiel.sem.idoneidade", "senhaForte123");

        mockMvc.perform(post("/api/v1/membros/promover-coordenador")
                        .with(authentication(getParocoAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Registra a idoneidade e informa quem conferiu o envio")
    void deveRegistrarEConsultarIdoneidade() throws Exception {
        UUID fielId = FielTestFixtures.criarFiel(fielRepository, "Fiel Idoneidade");
        UUID membroId = createMembro(fielId);

        mockMvc.perform(get("/api/v1/membros/" + membroId + "/idoneidade")
                        .with(authentication(getParocoAuth())))
                .andExpect(status().isOk());

        var request = new IdoneidadeRequest(LocalDate.of(2025, 1, 1), null, null, null, LocalDate.of(2025, 2, 1));
        mockMvc.perform(put("/api/v1/membros/" + membroId + "/idoneidade")
                        .with(authentication(getParocoAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enviadoEm").value("2025-02-01"))
                .andExpect(jsonPath("$.enviadoPor").isNotEmpty());
    }

    @Test
    @DisplayName("Vínculo inexistente retorna 400 e usuário comum não acessa a idoneidade")
    void deveNegarIdoneidadeSemPermissao() throws Exception {
        mockMvc.perform(get("/api/v1/membros/" + UUID.randomUUID() + "/idoneidade")
                        .with(authentication(getParocoAuth())))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/membros/" + UUID.randomUUID() + "/idoneidade")
                        .with(user("fiel").roles("USER")))
                .andExpect(status().isForbidden());
    }

    private void atestarIdoneidade(UUID membroId) throws Exception {
        var request = new IdoneidadeRequest(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 1), null, LocalDate.of(2025, 1, 1));
        mockMvc.perform(put("/api/v1/membros/" + membroId + "/idoneidade")
                        .with(authentication(getParocoAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private UUID createMembro(UUID fielId) {
        var entity = new MembroGrupoEntity();
        entity.setId(UUID.randomUUID());
        entity.setFielId(fielId);
        entity.setOrganizacaoId(orgId);
        entity.setTipoOrganizacao(TipoOrganizacao.PASTORAL);
        entity.setPapel("MEMBRO_ATIVO");
        entity.setAtivo(true);
        membroRepository.saveAndFlush(entity);
        return entity.getId();
    }
}
