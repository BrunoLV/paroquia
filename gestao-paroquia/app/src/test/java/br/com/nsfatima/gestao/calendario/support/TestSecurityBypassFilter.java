package br.com.nsfatima.gestao.calendario.support;

import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TestSecurityBypassFilter extends OncePerRequestFilter {

    private static final String TEST_ANONYMOUS_HEADER = "X-Test-Anonymous";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (shouldStayAnonymous(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (isAuthenticated(existing)) {
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            filterChain.doFilter(request, response);
            return;
        }
        if (isLoginRequest(request)
                || hasSecurityContextInSession(request)
                || hasSessionCookie(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String role = headerOrDefault(request, "X-Actor-Role", "coordenador");
        String organizationType = headerOrDefault(request, "X-Actor-Org-Type", "PASTORAL");
        UUID organizationId = resolveOrganizationId(request.getHeader("X-Actor-Org-Id"));
        UUID igrejaVinculoId = resolveOrganizationId(request.getHeader("X-Actor-Igreja-Id"));

        PapelGlobal papelGlobal = PapelGlobal.fromStoredValue(role);
        List<LeitorVinculoExterno.Vinculo> vinculos = (papelGlobal != null && organizationId == null)
                ? List.of()
                : List.of(new LeitorVinculoExterno.Vinculo(
                        organizationId,
                        "GrupoParoquial",
                        organizationType,
                        null,
                        null,
                        null,
                        true,
                        role,
                        igrejaVinculoId));
        List<String> papeisGlobais = papelGlobal != null ? List.of(papelGlobal.storedValue()) : List.of();

        UsuarioDetails principal = new UsuarioDetails(
                resolveUserId(role, organizationType),
                resolveUsername(role, organizationType),
                "{noop}senha123",
                true,
                null,
                vinculos,
                papeisGlobais);

        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal,
                null,
                principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", context);
        SecurityContextHolder.setContext(context);
        try {
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private boolean shouldStayAnonymous(HttpServletRequest request) {
        return "true".equalsIgnoreCase(request.getHeader(TEST_ANONYMOUS_HEADER));
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return "/api/v1/autenticacao/login".equals(request.getRequestURI());
    }

    private boolean hasSecurityContextInSession(HttpServletRequest request) {
        return request.getSession(false) != null
                && request.getSession(false).getAttribute("SPRING_SECURITY_CONTEXT") != null;
    }

    private boolean hasSessionCookie(HttpServletRequest request) {
        return request.getCookies() != null
                && Arrays.stream(request.getCookies())
                .anyMatch(cookie -> "JSESSIONID".equals(cookie.getName()));
    }

    private String headerOrDefault(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private UUID resolveOrganizationId(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawValue);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private UUID resolveUserId(String role, String organizationType) {
        String normalizedRole = role == null ? "" : role.trim().toLowerCase();
        String normalizedType = organizationType == null ? "" : organizationType.trim().toLowerCase();
        if ("membro-ativo".equals(normalizedRole) || "voluntario".equals(normalizedRole)) {
            return UUID.fromString("00000000-0000-0000-0000-000000000003");
        }
        if ("secretario-conselho".equals(normalizedRole)
                || "secretaria-paroquial".equals(normalizedRole)
                || "secretario-grupo".equals(normalizedRole)) {
            return UUID.fromString("00000000-0000-0000-0000-000000000002");
        }
        if ("paroco".equals(normalizedRole)
                || (("coordenador".equals(normalizedRole) || "membro-conselho-pastoral".equals(normalizedRole))
                        && ("conselho".equals(normalizedType)
                                || "cpc".equals(normalizedType)
                                || "cpp".equals(normalizedType)))) {
            return UUID.fromString("00000000-0000-0000-0000-000000000004");
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    private String resolveUsername(String role, String organizationType) {
        String normalizedRole = role == null ? "" : role.trim().toLowerCase();
        String normalizedType = organizationType == null ? "" : organizationType.trim().toLowerCase();
        if ("membro-ativo".equals(normalizedRole) || "voluntario".equals(normalizedRole)) {
            return "pedro.membro";
        }
        if ("secretario-conselho".equals(normalizedRole)
                || "secretaria-paroquial".equals(normalizedRole)
                || "secretario-grupo".equals(normalizedRole)) {
            return "maria.secretaria";
        }
        if ("paroco".equals(normalizedRole)
                || (("coordenador".equals(normalizedRole) || "membro-conselho-pastoral".equals(normalizedRole))
                        && ("conselho".equals(normalizedType)
                                || "cpc".equals(normalizedType)
                                || "cpp".equals(normalizedType)))) {
            return "ana.conselho";
        }
        return "joao.silva";
    }
}
