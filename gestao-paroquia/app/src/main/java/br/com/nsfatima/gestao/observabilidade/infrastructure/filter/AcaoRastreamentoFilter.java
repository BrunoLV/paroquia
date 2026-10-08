package br.com.nsfatima.gestao.observabilidade.infrastructure.filter;

import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro de rastreamento de ações HTTP executadas no sistema.
 *
 * <p>Executa após a resolução do contexto de segurança do Spring Security (@Order(0)),
 * extraindo a identidade do ator autenticado (sem expor PII) para enriquecer o contexto
 * do MDC e registrar métricas de início, conclusão, tempo de execução e status HTTP.
 *
 * <p>Garante a observância estrita da LGPD (PII Zero nos logs): registra apenas
 * identificadores técnicos, método, rota, duração e status.
 */
@Component
@Order(0)
public class AcaoRastreamentoFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcaoRastreamentoFilter.class);

    public static final String MDC_USUARIO_ID = "usuarioId";
    public static final String MDC_ORGANIZACAO_ID = "organizacaoId";
    public static final String MDC_ACAO = "acao";

    private static final String PREFIXO_ACTUATOR = "/actuator";

    /**
     * Isola endpoints operacionais e de telemetria (health check, prometheus e métricas)
     * do pipeline de rastreamento transacional para prevenir poluição dos registros de auditoria
     * e sobrecarga desnecessária em sondagens frequentes de orquestradores.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri != null) {
            String contextPath = request.getContextPath();
            String path = (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath))
                    ? uri.substring(contextPath.length())
                    : uri;
            if (path.startsWith(PREFIXO_ACTUATOR)) {
                return true;
            }
        }
        String servletPath = request.getServletPath();
        return servletPath != null && servletPath.startsWith(PREFIXO_ACTUATOR);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        long inicioMs = System.currentTimeMillis();
        String metodo = request.getMethod();
        String uri = request.getRequestURI();

        configurarMdcContexto(metodo, uri);
        registrarInicio(metodo, uri);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duracaoMs = System.currentTimeMillis() - inicioMs;
            registrarConclusao(metodo, uri, response.getStatus(), duracaoMs);
            limparMdcContexto();
        }
    }

    private void configurarMdcContexto(String metodo, String uri) {
        String usuarioId = resolverUsuarioId();
        String organizacaoId = resolverOrganizacaoId();
        String acao = resolverAcao(metodo, uri);

        MDC.put(MDC_USUARIO_ID, usuarioId);
        MDC.put(MDC_ACAO, acao);
        if (organizacaoId != null) {
            MDC.put(MDC_ORGANIZACAO_ID, organizacaoId);
        }
    }

    private void registrarInicio(String metodo, String uri) {
        LOGGER.info("[HTTP_REQ] INICIO metodo={} uri={}", metodo, uri);
    }

    private void registrarConclusao(String metodo, String uri, int status, long duracaoMs) {
        LOGGER.info("[HTTP_REQ] CONCLUIDO metodo={} uri={} status={} duracaoMs={}",
                metodo, uri, status, duracaoMs);
    }

    private void limparMdcContexto() {
        MDC.remove(MDC_USUARIO_ID);
        MDC.remove(MDC_ORGANIZACAO_ID);
        MDC.remove(MDC_ACAO);
    }

    private String resolverUsuarioId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetails user) {
            UUID id = user.getUsuarioId();
            return id != null ? id.toString() : "ANONYMOUS";
        }
        return "ANONYMOUS";
    }

    private String resolverOrganizacaoId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioDetails user) {
            return user.vinculoPrimario()
                    .map(LeitorVinculoExterno.Vinculo::organizationId)
                    .map(UUID::toString)
                    .orElse(null);
        }
        return null;
    }

    private String resolverAcao(String metodo, String uri) {
        if (uri == null || uri.isBlank()) {
            return metodo != null ? metodo : "DESCONHECIDO";
        }
        return (metodo != null ? metodo : "REQ") + " " + uri;
    }
}
