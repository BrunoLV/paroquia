package br.com.nsfatima.gestao.dashboard.integration;

import br.com.nsfatima.gestao.calendario.support.SecurityTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class DashboardComunidadeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveNegarAniversariantesParaNaoAutenticado() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/v1/dashboard/aniversariantes").header("X-Test-Anonymous", "true"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarAniversariantesComoArray() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/aniversariantes").param("janela", "MES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarRetencaoDePastoraisComoArray() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/comunidade"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retencaoPastorais").isArray());
    }

    @Test
    void deveNegarComunidadeDeOutraOrganizacaoParaNaoAdmin() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "joao.silva", "senha123");

        mockMvc.perform(get("/api/v1/dashboard/comunidade")
                        .session(session)
                        .param("grupoParoquialId", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());
    }
}
