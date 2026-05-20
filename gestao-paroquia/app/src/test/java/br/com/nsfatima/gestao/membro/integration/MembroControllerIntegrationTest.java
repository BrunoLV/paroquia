package br.com.nsfatima.gestao.membro.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.iam.infrastructure.security.ExternalMembershipReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
        UUID userId = UUID.randomUUID();
        UsuarioDetails details = new UsuarioDetails(
                userId, "admin", "pass", true, "ROLE_ADMIN",
                List.of(new ExternalMembershipReader.Membership(ORG_ID, "PASTORAL", "coordenador")));
        
        return UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
    }

    @Test
    @DisplayName("Deve criar um membro e registrar auditoria")
    void deveCriarMembroERegistrarAuditoria() throws Exception {
        MembroRequest request = new MembroRequest(
                "João da Silva",
                LocalDate.of(1990, 1, 1),
                "joao@example.com",
                null, null, null, null, null, null, null
        );

        mockMvc.perform(post("/api/v1/membros")
                .with(authentication(getAdminAuth()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCompleto").value("João da Silva"));

        assertThat(membroRepository.findAll()).hasSize(1);
        assertThat(auditoriaRepository.findAll()).hasSize(1);
        assertThat(auditoriaRepository.findAll().getFirst().getRecursoTipo()).isEqualTo("MEMBRO");
        assertThat(auditoriaRepository.findAll().getFirst().getOrganizacaoId()).isEqualTo(ORG_ID);
    }

    @Test
    @DisplayName("Deve listar membros com filtros")
    void deveListarMembrosComFiltros() throws Exception {
        createMembro("Alice");
        createMembro("Bob");

        mockMvc.perform(get("/api/v1/membros")
                .with(authentication(getAdminAuth()))
                .param("nome", "Alice"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nomeCompleto").value("Alice"));
    }

    @Test
    @DisplayName("Deve adicionar participação em organização")
    void deveAdicionarParticipacao() throws Exception {
        var membroId = UUID.randomUUID();
        var entity = new br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity();
        entity.setId(membroId);
        entity.setNomeCompleto("Membro Teste");
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

    private void createMembro(String nome) {
        var entity = new br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity();
        entity.setId(UUID.randomUUID());
        entity.setNomeCompleto(nome);
        entity.setAtivo(true);
        membroRepository.saveAndFlush(entity);
    }
}
