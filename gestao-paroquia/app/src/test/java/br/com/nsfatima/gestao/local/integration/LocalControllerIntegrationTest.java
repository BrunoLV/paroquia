package br.com.nsfatima.gestao.local.integration;

import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.local.api.v1.dto.LocalRequest;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LocalControllerIntegrationTest {

    private static final UUID ORG_ID = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private LocalJpaRepository localRepository;
    @Autowired
    private br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository
            .UnidadeTerritorialJpaRepository unidadeTerritorialRepository;
    @Autowired
    private ObjectMapper objectMapper;
    private UUID existingLocalId;

    @BeforeEach
    void setUp() {
        localRepository.deleteAll();

        br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity org =
                unidadeTerritorialRepository.findById(ORG_ID)
                        .orElseGet(br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity
                                .UnidadeTerritorialEntity::new);
        org.setId(ORG_ID);
        org.setNome("Comunidade Matriz Teste");
        org.setTipo(br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial.MATRIZ);
        org.setAtivo(true);
        unidadeTerritorialRepository.save(org);

        LocalEntity local = new LocalEntity();
        local.setId(UUID.randomUUID());
        local.setNome("Igreja Matriz");
        local.setTipo("IGREJA");
        local.setCapacidade(500);
        local.setAtivo(true);
        localRepository.save(local);
        existingLocalId = local.getId();
    }

    private Authentication getAuthForRole(String role) {
        UUID userId = UUID.randomUUID();
        UsuarioDetails details = new UsuarioDetails(
                userId, "user", "pass", true, "ROLE_" + role,
                List.of(new LeitorVinculoExterno.Vinculo(ORG_ID, "PASTORAL", "membro")));
        return UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
    }

    @Test
    @DisplayName("Permitir listagem e detalhes para qualquer usuário autenticado")
    void devePermitirListagemEDetalhesParaQualquerAutenticado() throws Exception {
        Authentication memberAuth = getAuthForRole("PASTORAL_MEMBRO");

        mockMvc.perform(get("/api/v1/locais")
                        .with(authentication(memberAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nome").value("Igreja Matriz"));

        mockMvc.perform(get("/api/v1/locais/" + existingLocalId)
                        .with(authentication(memberAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Igreja Matriz"));
    }

    @Test
    @DisplayName("Permitir POST/PUT/DELETE para ADMIN, Pároco e Coordenadores/Vice do Conselho")
    void devePermitirMutacoesParaPerfisAutorizados() throws Exception {
        String[] authorizedRoles = {"ADMIN", "CLERO_PAROCO", "CONSELHO_COORDENADOR", "CONSELHO_VICE_COORDENADOR"};

        for (String role : authorizedRoles) {
            Authentication auth = getAuthForRole(role);

            // POST
            LocalRequest request = new LocalRequest("Salão " + role, "SALAO", 200, "Wifi", true);
            mockMvc.perform(post("/api/v1/locais")
                            .with(authentication(auth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // PUT
            LocalRequest updateRequest = new LocalRequest("Igreja Matriz Atualizada",
                    "IGREJA", 600, "Som", true);

            mockMvc.perform(put("/api/v1/locais/" + existingLocalId)
                            .with(authentication(auth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk());

            // DELETE (Soft delete)
            mockMvc.perform(delete("/api/v1/locais/" + existingLocalId)
                            .with(authentication(auth)))
                    .andExpect(status().isNoContent());

            // Garantir soft delete: inativo e com deletedAt preenchido
            LocalEntity local = localRepository.findById(existingLocalId).orElseThrow();
            assertThat(local.isAtivo()).isFalse();
            assertThat(local.getDeletedAt()).isNotNull();

            // Re-ativar para a próxima iteração
            local.setAtivo(true);
            local.setDeletedAt(null);
            localRepository.save(local);
        }
    }

    @Test
    @DisplayName("Permitir criação e edição enviando organizacaoId como alias no JSON")
    void devePermitirCriacaoEEdicaoComOrganizacaoIdNoPayload() throws Exception {
        Authentication auth = getAuthForRole("ADMIN");

        String jsonPayload = """
                {
                    "nome": "Salão Comunitário Teste",
                    "tipo": "SALAO",
                    "organizacaoId": "%s",
                    "capacidadeMaxima": 150,
                    "descricao": "Espaço amplo com organizacaoId",
                    "ativo": true
                }
                """.formatted(ORG_ID);

        mockMvc.perform(put("/api/v1/locais/" + existingLocalId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Salão Comunitário Teste"))
                .andExpect(jsonPath("$.capacidadeMaxima").value(150))
                .andExpect(jsonPath("$.organizacaoId").value(ORG_ID.toString()))
                .andExpect(jsonPath("$.igrejaId").value(ORG_ID.toString()))
                .andExpect(jsonPath("$.igrejaNome").value("Comunidade Matriz Teste"))
                .andExpect(jsonPath("$.organizacaoNome").value("Comunidade Matriz Teste"));
    }

    @Test
    @DisplayName("Negar POST/PUT/DELETE para perfis não autorizados")
    void deveNegarMutacoesParaPerfisNaoAutorizados() throws Exception {
        String[] unauthorizedRoles = {"PASTORAL_MEMBRO", "PASTORAL_COORDENADOR", "CLERO_VIGARIO"};

        for (String role : unauthorizedRoles) {
            Authentication auth = getAuthForRole(role);

            // POST
            LocalRequest request = new LocalRequest("Novo Local Negado", "SALAO", 200, "Wifi", true);
            mockMvc.perform(post("/api/v1/locais")
                            .with(authentication(auth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            // PUT
            mockMvc.perform(put("/api/v1/locais/" + existingLocalId)
                            .with(authentication(auth))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            // DELETE
            mockMvc.perform(delete("/api/v1/locais/" + existingLocalId)
                            .with(authentication(auth)))
                    .andExpect(status().isForbidden());
        }
    }
}
