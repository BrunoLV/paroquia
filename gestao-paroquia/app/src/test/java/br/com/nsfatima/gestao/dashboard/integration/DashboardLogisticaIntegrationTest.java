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
class DashboardLogisticaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveNegarNaoAutenticadoAcesso() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/v1/painel/logistica").header("X-Test-Anonymous", "true"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveRetornarEstruturaDeLogisticaParaAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/painel/logistica").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.choquesHorario").isArray())
                .andExpect(jsonPath("$.ocupacaoAmbientes.taxaDiaPercentual").exists())
                .andExpect(jsonPath("$.ocupacaoAmbientes.taxaSemanaPercentual").exists())
                .andExpect(jsonPath("$.ocupacaoAmbientes.porLocal").isArray())
                .andExpect(jsonPath("$.eventosPorResponsavel").isArray())
                .andExpect(jsonPath("$.pendenciasHomologacao").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deveAceitarRecorteTemporalNoFiltro() throws Exception {
        mockMvc.perform(get("/api/v1/painel/logistica").param("periodo", "MES"))
                .andExpect(status().isOk());
    }

    @Test
    void deveNegarNaoAdminQueSolicitaOutraComunidadeNoFiltro() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "joao.silva", "senha123");

        mockMvc.perform(get("/api/v1/painel/logistica")
                        .session(session)
                        .param("organizacaoId", UUID.randomUUID().toString()))
                .andExpect(status().isForbidden());
    }
}
