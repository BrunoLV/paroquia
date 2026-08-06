package br.com.nsfatima.gestao.iam.integration;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.support.FielTestFixtures;
import br.com.nsfatima.gestao.iam.api.v1.dto.PapelGlobalRequest;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.PapelGlobalJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PapelGlobalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private FielJpaRepository fielRepository;
    @Autowired
    private PapelGlobalJpaRepository papelGlobalRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveConcederPapelGlobalComoAdmin() throws Exception {
        UUID fielId = FielTestFixtures.criarFiel(fielRepository, "Vigário Teste");
        var request = new PapelGlobalRequest(fielId, "VIGARIO");

        mockMvc.perform(post("/api/v1/papeis-globais")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        assertThat(papelGlobalRepository.findByFielIdAndAtivoTrue(fielId)).hasSize(1);
    }

    @Test
    void deveRevogarPapelGlobalComoAdmin() throws Exception {
        UUID fielId = FielTestFixtures.criarFiel(fielRepository, "Pároco Teste");
        var entity = new br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.PapelGlobalEntity();
        entity.setId(UUID.randomUUID());
        entity.setFielId(fielId);
        entity.setPapel("paroco");
        entity.setAtivo(true);
        papelGlobalRepository.saveAndFlush(entity);

        var request = new PapelGlobalRequest(fielId, "PAROCO");

        mockMvc.perform(post("/api/v1/papeis-globais/revogar")
                        .with(org.springframework.security.test.web.servlet.request
                                .SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        assertThat(papelGlobalRepository.findByFielIdAndAtivoTrue(fielId)).isEmpty();
    }

    @Test
    @WithMockUser(roles = "USER")
    void deveNegarAcessoParaNaoAdmin() throws Exception {
        var request = new PapelGlobalRequest(UUID.randomUUID(), "VIGARIO");

        mockMvc.perform(post("/api/v1/papeis-globais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
