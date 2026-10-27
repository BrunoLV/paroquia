package br.com.nsfatima.gestao.calendario.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/**
 * Permite que o Prometheus raspe {@code GET /actuator/prometheus} sem sessão de usuário, usando um
 * token Bearer dedicado. Existe porque as métricas deixaram de ser públicas e o Prometheus não faz
 * login por formulário. O token concede um papel mínimo ({@value #PAPEL_SCRAPER}) válido somente
 * para esse caminho; o contexto de segurança vale apenas para a requisição (não é gravado na
 * sessão). Token vazio desativa o filtro, deixando as métricas acessíveis só a ADMIN.
 */
public class ActuatorScrapeTokenFilter extends OncePerRequestFilter {

    public static final String PAPEL_SCRAPER = "METRICS_SCRAPER";

    private static final String CAMINHO = "/actuator/prometheus";
    private static final String PREFIXO_BEARER = "Bearer ";

    private final byte[] token;

    public ActuatorScrapeTokenFilter(String token) {
        this.token = token == null ? new byte[0] : token.trim().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return token.length == 0 || !CAMINHO.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecalho != null && cabecalho.startsWith(PREFIXO_BEARER)
                && MessageDigest.isEqual(
                        token, cabecalho.substring(PREFIXO_BEARER.length()).trim().getBytes(StandardCharsets.UTF_8))) {
            SecurityContext contexto = SecurityContextHolder.createEmptyContext();
            contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                    "prometheus", null, List.of(new SimpleGrantedAuthority("ROLE_" + PAPEL_SCRAPER))));
            SecurityContextHolder.setContext(contexto);
        }
        filterChain.doFilter(request, response);
    }
}
