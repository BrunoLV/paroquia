package br.com.nsfatima.gestao.calendario.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre a superfície endurecida: observabilidade e documentação restritas, ingestão de logs
 * autenticada, rejeição de origem não autorizada em métodos mutáveis e cabeçalhos de segurança.
 * Requisições anônimas usam {@code X-Test-Anonymous} porque o suporte de testes autentica por padrão.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class HardeningSegurancaIntegrationTest {

    private static final String ANONIMO = "X-Test-Anonymous";
    private static final String TOKEN_SCRAPE = "token-de-scrape-exclusivo-para-testes";
    private static final String LOG_VALIDO = "{\"logs\":[{\"nivel\":\"INFO\",\"mensagem\":\"ok\"}]}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Health e info permanecem públicos")
    void healthDevePermanecerPublico() throws Exception {
        mockMvc.perform(get("/actuator/health").header(ANONIMO, "true")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Métricas e prometheus anônimos devem ser negados")
    void metricasAnonimasDevemSerNegadas() throws Exception {
        mockMvc.perform(get("/actuator/prometheus").header(ANONIMO, "true")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/metrics").header(ANONIMO, "true")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Scrape do Prometheus deve aceitar apenas o token dedicado e somente em /actuator/prometheus")
    void scrapeTokenDeveSerEscopado() throws Exception {
        mockMvc.perform(get("/actuator/prometheus").header(ANONIMO, "true")
                        .header("Authorization", "Bearer " + TOKEN_SCRAPE))
                .andExpect(status().isOk());
        mockMvc.perform(get("/actuator/prometheus").header(ANONIMO, "true")
                        .header("Authorization", "Bearer token-errado"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/metrics").header(ANONIMO, "true")
                        .header("Authorization", "Bearer " + TOKEN_SCRAPE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN autenticado acessa métricas")
    void adminAcessaMetricas() throws Exception {
        mockMvc.perform(get("/actuator/metrics")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Usuário sem papel ADMIN não acessa métricas nem documentação")
    void naoAdminNaoAcessaMetricas() throws Exception {
        mockMvc.perform(get("/actuator/metrics")).andExpect(status().isForbidden());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OpenAPI e Swagger anônimos devem ser negados")
    void swaggerAnonimoDeveSerNegado() throws Exception {
        mockMvc.perform(get("/v3/api-docs").header(ANONIMO, "true")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/swagger-ui/index.html").header(ANONIMO, "true")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Ingestão de logs exige autenticação")
    void ingestaoDeLogsExigeAutenticacao() throws Exception {
        mockMvc.perform(post("/api/v1/observabilidade/logs").header(ANONIMO, "true")
                        .contentType(MediaType.APPLICATION_JSON).content(LOG_VALIDO))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/observabilidade/logs")
                        .contentType(MediaType.APPLICATION_JSON).content(LOG_VALIDO))
                .andExpect(status().isAccepted());
    }

    @Test
    @DisplayName("Requisição mutável de origem não autorizada deve ser rejeitada com 403")
    void origemNaoAutorizadaDeveSerRejeitada() throws Exception {
        String corpo = "{\"username\":\"origem." + UUID.randomUUID() + "\",\"password\":\"x\"}";

        mockMvc.perform(post("/api/v1/autenticacao/login").header("Origin", "http://evil.example")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/autenticacao/login").header("Origin", "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Respostas devem carregar CSP, HSTS (HTTPS), Referrer-Policy e nosniff")
    void deveEmitirCabecalhosDeSeguranca() throws Exception {
        mockMvc.perform(get("/actuator/health").secure(true).header(ANONIMO, "true"))
                .andExpect(header().exists("Content-Security-Policy"))
                .andExpect(header().exists("Strict-Transport-Security"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }
}
