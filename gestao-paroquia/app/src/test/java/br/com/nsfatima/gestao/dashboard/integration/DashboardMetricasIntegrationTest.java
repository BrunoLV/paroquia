package br.com.nsfatima.gestao.dashboard.integration;

import br.com.nsfatima.gestao.calendario.support.SecurityTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
class DashboardMetricasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveNegarNaoAutenticadoAcesso() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/v1/painel/metricas")
                        .header("X-Test-Anonymous", "true"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarDashboardMetricasParaAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/painel/metricas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumoGeral.totalEventosPeriodo").exists())
                .andExpect(jsonPath("$.resumoGeral.totalFieisAtivos").exists())
                .andExpect(jsonPath("$.resumoGeral.novosFieisMes").exists())
                .andExpect(jsonPath("$.resumoGeral.fieisEngajadosPastoral").exists())
                .andExpect(jsonPath("$.resumoGeral.fieisSemVinculoPastoral").exists())
                .andExpect(jsonPath("$.eventosPorOrganizacao").isArray())
                .andExpect(jsonPath("$.projetosPorStatus.planejamento").exists())
                .andExpect(jsonPath("$.projetosPorStatus.emExecucao").exists())
                .andExpect(jsonPath("$.projetosPorStatus.concluidos").exists())
                .andExpect(jsonPath("$.projetosPorStatus.atrasados").exists())
                .andExpect(jsonPath("$.projetosAtivosDetalhes").isArray())
                .andExpect(jsonPath("$.eventosOrfaos").isArray())
                .andExpect(jsonPath("$.eventosSemana").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAceitarRecorteTemporalNoFiltro() throws Exception {
        mockMvc.perform(get("/api/v1/painel/metricas")
                        .param("periodo", "MES")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumoGeral").exists());
    }

    @Test
    void deveRetornarFiltradoMetricasParaNaoAdmin() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "joao.silva", "senha123");

        mockMvc.perform(get("/api/v1/painel/metricas")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumoGeral").exists())
                .andExpect(jsonPath("$.eventosPorOrganizacao").isArray());
    }

    @Test
    void deveNegarNaoAdminQueSolicitaOutraComunidadeNoFiltro() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "joao.silva", "senha123");

        mockMvc.perform(get("/api/v1/painel/metricas")
                        .session(session)
                        .param("organizacaoId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
