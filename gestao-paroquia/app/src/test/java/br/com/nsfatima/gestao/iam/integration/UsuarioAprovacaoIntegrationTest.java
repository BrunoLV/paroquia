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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioAprovacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAprovarUsuarioQuandoAutorizado() throws Exception {
        UsuarioEntity user = new UsuarioEntity();
        user.setId(UUID.randomUUID());
        user.setUsername("user.to.approve." + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("hash");
        user.setEnabled(false);
        user.setRoles("ROLE_USER");
        usuarioRepository.save(user);

        mockMvc.perform(put("/api/v1/usuarios/" + user.getId() + "/approve")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        UsuarioEntity updated = usuarioRepository.findById(user.getId()).orElseThrow();
        assertTrue(updated.isEnabled());
        assertTrue(updated.getRoles().contains("ROLE_USER"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarAprovarUsuarioQuandoNaoAutorizado() throws Exception {
        mockMvc.perform(put("/api/v1/usuarios/" + UUID.randomUUID() + "/approve")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
