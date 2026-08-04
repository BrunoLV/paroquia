package br.com.nsfatima.gestao.iam.integration;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class UsuarioAdminApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAlternarUsuarioStatus() throws Exception {
        UsuarioEntity user = new UsuarioEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("user_to_disable");
        user.setPasswordHash("hash");
        user.setEnabled(true);
        usuarioRepository.save(user);

        mockMvc.perform(patch("/api/v1/usuarios/" + user.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\": false}"))
                .andExpect(status().isNoContent());

        UsuarioEntity updated = usuarioRepository.findById(user.getId()).orElseThrow();
        System.out.println("USER ENABLED STATUS: " + updated.isEnabled());
        assertFalse(updated.isEnabled());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarAdminActionParaRegularUsuario() throws Exception {
        mockMvc.perform(patch("/api/v1/usuarios/" + UUID.randomUUID() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\": false}"))
                .andExpect(status().isForbidden());
    }
}
