package br.com.nsfatima.gestao.dashboard.integration;

import br.com.nsfatima.gestao.calendario.support.SecurityTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class DashboardMetricsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldDenyUnauthenticatedAccess() throws Exception {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/v1/dashboard/metrics")
                .header("X-Test-Anonymous", "true"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnDashboardMetricsForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/metrics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumoGeral").exists())
                .andExpect(jsonPath("$.eventosPorOrganizacao").isArray())
                .andExpect(jsonPath("$.projetosAtivosDetalhes").isArray())
                .andExpect(jsonPath("$.eventosSemana").isArray());
    }

    @Test
    void shouldReturnFilteredMetricsForNonAdmin() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "joao.silva", "senha123");

        mockMvc.perform(get("/api/v1/dashboard/metrics")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumoGeral").exists())
                .andExpect(jsonPath("$.eventosPorOrganizacao").isArray());
    }
}
