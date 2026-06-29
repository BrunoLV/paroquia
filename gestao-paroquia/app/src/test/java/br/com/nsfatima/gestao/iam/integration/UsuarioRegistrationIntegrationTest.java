package br.com.nsfatima.gestao.iam.integration;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldRegisterNewUserAsDisabledAndEncrypted() throws Exception {
        String username = "test.register." + UUID.randomUUID() + "@example.com";
        String body = """
                {
                    "nome": "João da Silva",
                    "username": "%s",
                    "password": "SecretPassword123",
                    "telefone": "11988888888",
                    "dataNascimento": "1990-05-15",
                    "organizacoesSolicitadas": []
                }
                """.formatted(username);

        mockMvc.perform(post("/api/v1/usuarios/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // Verify in DB via JPA (should decrypt automatically)
        UsuarioEntity user = usuarioRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new AssertionError("User should have been saved"));

        assertFalse(user.isEnabled());
        assertEquals("João da Silva", user.getNome());
        assertEquals("11988888888", user.getTelefone());
        assertEquals("1990-05-15", user.getDataNascimento().toString());

        // Verify in DB via raw SQL (should be encrypted)
        String rawNome = jdbcTemplate.queryForObject(
                "SELECT nome FROM calendario.usuarios WHERE id = ?",
                String.class,
                user.getId());

        assertNotNull(rawNome);
        assertNotEquals("João da Silva", rawNome);
    }

    @Autowired
    private br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository organizacaoJpaRepository;

    @Test
    void shouldFailRegistrationWhenPastoralIsRequestedWithoutIgrejaVinculo() throws Exception {
        br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity org = new br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity();
        UUID orgId = UUID.randomUUID();
        org.setId(orgId);
        org.setNome("Pastoral Teste Validation " + UUID.randomUUID());
        org.setTipo(br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao.PASTORAL);
        org.setAtivo(true);
        organizacaoJpaRepository.save(org);

        String username = "test.fail.reg." + UUID.randomUUID() + "@example.com";
        String body = """
                {
                    "nome": "João da Silva",
                    "username": "%s",
                    "password": "SecretPassword123",
                    "telefone": "11988888888",
                    "dataNascimento": "1990-05-15",
                    "organizacoesSolicitadas": [
                        {
                            "organizacaoId": "%s",
                            "igrejaVinculoId": null
                        }
                    ]
                }
                """.formatted(username, orgId);

        mockMvc.perform(post("/api/v1/usuarios/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
