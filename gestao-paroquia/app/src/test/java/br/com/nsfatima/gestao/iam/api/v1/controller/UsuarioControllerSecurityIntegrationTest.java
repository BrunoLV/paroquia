package br.com.nsfatima.gestao.iam.api.v1.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void devePermitirAdminParaAcessoUsuarios() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarNaoAdminParaAcessoUsuarios() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VIGARIO")
    void devePermitirVigarioParaAcessoUsuarios() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    void deveExigirAuthParaAcessoUsuarios() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios").header("X-Test-Anonymous", "true"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PAROCO")
    void devePermitirParocoCriarUsuario() throws Exception {
        String payload = """
                {
                    "username": "usuario.paroco.teste",
                    "password": "senhaForte123!",
                    "fielId": "00000000-0000-0000-0000-000000000001"
                }
                """;
        // Não deve retornar 403 (Forbidden); retorna 400 pois o Fiel ID não existe no banco
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SECRETARIA_PAROQUIAL")
    void devePermitirSecretariaCriarUsuario() throws Exception {
        String payload = """
                {
                    "username": "usuario.secretaria.teste",
                    "password": "senhaForte123!",
                    "fielId": "00000000-0000-0000-0000-000000000001"
                }
                """;
        // Não deve retornar 403 (Forbidden); retorna 400 pois o Fiel ID não existe no banco
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarUsuarioComumCriarUsuario() throws Exception {
        String payload = """
                {
                    "username": "usuario.comum.teste",
                    "password": "senhaForte123!",
                    "fielId": "00000000-0000-0000-0000-000000000001"
                }
                """;
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PAROCO")
    void devePermitirParocoAprovarUsuario() throws Exception {
        UUID id = UUID.randomUUID();
        // 404 indica que passou pelo @PreAuthorize do Paroco
        mockMvc.perform(put("/api/v1/usuarios/" + id + "/aprovar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SECRETARIA_PAROQUIAL")
    void deveNegarSecretariaAprovarUsuario() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/usuarios/" + id + "/aprovar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "COORDENADOR")
    void deveNegarCoordenadorAprovarUsuario() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/usuarios/" + id + "/aprovar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAceitarPayloadVinculoComAliasOrganizacaoId() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        String payload = """
                {
                    "organizacaoId": "%s",
                    "tipo": "PASTORAL",
                    "papel": "MEMBRO_ATIVO",
                    "igrejaVinculoId": "%s"
                }
                """.formatted(orgId, UUID.randomUUID());

        mockMvc.perform(post("/api/v1/usuarios/" + usuarioId + "/membros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Usuario nao encontrado")));
    }
}
