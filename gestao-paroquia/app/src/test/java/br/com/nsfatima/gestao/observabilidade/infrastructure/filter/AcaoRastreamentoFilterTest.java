package br.com.nsfatima.gestao.observabilidade.infrastructure.filter;

import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcaoRastreamentoFilterTest {

    private AcaoRastreamentoFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AcaoRastreamentoFilter();
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    @DisplayName("Deve configurar MDC com usuario anonimo e limpar no encerramento")
    void deveConfigurarMdcAnonimoELimpar() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/eventos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> capturouUsuario = new AtomicReference<>();
        AtomicReference<String> capturouAcao = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            capturouUsuario.set(MDC.get(AcaoRastreamentoFilter.MDC_USUARIO_ID));
            capturouAcao.set(MDC.get(AcaoRastreamentoFilter.MDC_ACAO));
        };

        filter.doFilter(request, response, chain);

        assertThat(capturouUsuario.get()).isEqualTo("ANONYMOUS");
        assertThat(capturouAcao.get()).isEqualTo("GET /api/v1/eventos");
        assertThat(MDC.get(AcaoRastreamentoFilter.MDC_USUARIO_ID)).isNull();
        assertThat(MDC.get(AcaoRastreamentoFilter.MDC_ACAO)).isNull();
    }

    @Test
    @DisplayName("Deve capturar usuarioId e organizacaoId quando autenticado")
    void deveCapturarUsuarioAutenticado() throws ServletException, IOException {
        UUID usuarioId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        LeitorVinculoExterno.Vinculo vinculo = new LeitorVinculoExterno.Vinculo(
                orgId, "PASTORAL", "COORDENADOR"
        );
        UsuarioDetails details = new UsuarioDetails(
                usuarioId, "padre.joao", "hash", true, "ADMIN", List.of(vinculo)
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/eventos");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<String> capturouUsuario = new AtomicReference<>();
        AtomicReference<String> capturouGrupo = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            capturouUsuario.set(MDC.get(AcaoRastreamentoFilter.MDC_USUARIO_ID));
            capturouGrupo.set(MDC.get(AcaoRastreamentoFilter.MDC_ORGANIZACAO_ID));
        };

        filter.doFilter(request, response, chain);

        assertThat(capturouUsuario.get()).isEqualTo(usuarioId.toString());
        assertThat(capturouGrupo.get()).isEqualTo(orgId.toString());
        assertThat(MDC.get(AcaoRastreamentoFilter.MDC_USUARIO_ID)).isNull();
        assertThat(MDC.get(AcaoRastreamentoFilter.MDC_ORGANIZACAO_ID)).isNull();
    }

    @Test
    @DisplayName("Deve garantir limpeza do MDC mesmo quando o filterChain lancar excecao")
    void deveLimparMdcMesmoComExcecao() {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/v1/eventos/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain failingChain = (req, res) -> {
            throw new ServletException("Erro simulado na cadeia");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, failingChain))
                .isInstanceOf(ServletException.class)
                .hasMessage("Erro simulado na cadeia");

        assertThat(MDC.get(AcaoRastreamentoFilter.MDC_USUARIO_ID)).isNull();
        assertThat(MDC.get(AcaoRastreamentoFilter.MDC_ACAO)).isNull();
    }

    @Test
    @DisplayName("Nao deve rastrear ou poluir MDC em requisicoes direcionadas ao Actuator")
    void deveIgnorarRequisicoesActuator() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean executouChain = new AtomicBoolean(false);
        AtomicReference<String> capturouAcao = new AtomicReference<>();
        AtomicReference<String> capturouUsuario = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            executouChain.set(true);
            capturouAcao.set(MDC.get(AcaoRastreamentoFilter.MDC_ACAO));
            capturouUsuario.set(MDC.get(AcaoRastreamentoFilter.MDC_USUARIO_ID));
        };

        filter.doFilter(request, response, chain);

        assertThat(executouChain.get()).isTrue();
        assertThat(capturouAcao.get()).isNull();
        assertThat(capturouUsuario.get()).isNull();
        assertThat(MDC.get(AcaoRastreamentoFilter.MDC_ACAO)).isNull();
        assertThat(MDC.get(AcaoRastreamentoFilter.MDC_USUARIO_ID)).isNull();
    }

    @Test
    @DisplayName("Nao deve rastrear requisicoes do Actuator mesmo quando houver context-path")
    void deveIgnorarRequisicoesActuatorComContextPath() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/paroquia/actuator/prometheus");
        request.setContextPath("/paroquia");
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicBoolean executouChain = new AtomicBoolean(false);
        AtomicReference<String> capturouAcao = new AtomicReference<>();

        FilterChain chain = (req, res) -> {
            executouChain.set(true);
            capturouAcao.set(MDC.get(AcaoRastreamentoFilter.MDC_ACAO));
        };

        filter.doFilter(request, response, chain);

        assertThat(executouChain.get()).isTrue();
        assertThat(capturouAcao.get()).isNull();
        assertThat(MDC.get(AcaoRastreamentoFilter.MDC_ACAO)).isNull();
    }
}
