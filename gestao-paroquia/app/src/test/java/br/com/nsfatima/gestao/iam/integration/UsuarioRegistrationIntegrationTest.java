package br.com.nsfatima.gestao.iam.integration;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrganizacaoJpaRepository organizacaoJpaRepository;

    @Test
    void deveRegistrarNovoUsuarioComoDesabilitadoEEncrypted() throws Exception {
        OrganizacaoEntity igreja = new OrganizacaoEntity();
        UUID igrejaId = UUID.randomUUID();
        igreja.setId(igrejaId);
        igreja.setNome("Matriz Teste " + UUID.randomUUID());
        igreja.setTipo(TipoOrganizacao.MATRIZ);
        igreja.setAtivo(true);
        organizacaoJpaRepository.save(igreja);

        String username = "test.register." + UUID.randomUUID() + "@example.com";
        String body = """
                {
                    "nome": "João da Silva",
                    "username": "%s",
                    "password": "SecretPassword123",
                    "telefone": "11988888888",
                    "dataNascimento": "1990-05-15",
                    "igrejaId": "%s"
                }
                """.formatted(username, igrejaId);

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

    @Test
    void deveFalhaRegistrationQuandoIgrejaEFaltando() throws Exception {
        String username = "test.fail.reg." + UUID.randomUUID() + "@example.com";
        String body = """
                {
                    "nome": "João da Silva",
                    "username": "%s",
                    "password": "SecretPassword123",
                    "telefone": "11988888888",
                    "dataNascimento": "1990-05-15"
                }
                """.formatted(username);

        mockMvc.perform(post("/api/v1/usuarios/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
