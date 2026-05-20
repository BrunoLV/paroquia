package br.com.nsfatima.gestao.calendario.support;

import br.com.nsfatima.gestao.iam.infrastructure.security.ExternalMembershipReader;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TestSecurityBypassFilter extends OncePerRequestFilter {

    private static final String TEST_ANONYMOUS_HEADER = "X-Test-Anonymous";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        Authentication existing = SecurityContextHolder.getContext().getAuthentication();
        if (shouldStayAnonymous(request)
                || isAuthenticated(existing)
                || isLoginRequest(request)
                || hasSecurityContextInSession(request)
                || hasSessionCookie(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String role = headerOrDefault(request, "X-Actor-Role", "coordenador");
        String organizationType = headerOrDefault(request, "X-Actor-Org-Type", "PASTORAL");
        UUID organizationId = resolveOrganizationId(request.getHeader("X-Actor-Org-Id"));

        UsuarioDetails principal = new UsuarioDetails(
                resolveUserId(role, organizationType),
                resolveUsername(role, organizationType),
                "{noop}senha123",
                true,
                null,
                List.of(new ExternalMembershipReader.Membership(organizationId, organizationType, role)));

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
        return "/api/v1/auth/login".equals(request.getRequestURI());
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
        if ("membro".equals(normalizedRole)) {
            return UUID.fromString("00000000-0000-0000-0000-000000000003");
        }
        if ("secretario".equals(normalizedRole) && "conselho".equals(normalizedType)) {
            return UUID.fromString("00000000-0000-0000-0000-000000000002");
        }
        if (("coordenador".equals(normalizedRole) || "vice-coordenador".equals(normalizedRole))
                && "conselho".equals(normalizedType)) {
            return UUID.fromString("00000000-0000-0000-0000-000000000004");
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }

    private String resolveUsername(String role, String organizationType) {
        String normalizedRole = role == null ? "" : role.trim().toLowerCase();
        String normalizedType = organizationType == null ? "" : organizationType.trim().toLowerCase();
        if ("membro".equals(normalizedRole)) {
            return "pedro.membro";
        }
        if ("secretario".equals(normalizedRole) && "conselho".equals(normalizedType)) {
            return "maria.secretaria";
        }
        if (("coordenador".equals(normalizedRole) || "vice-coordenador".equals(normalizedRole))
                && "conselho".equals(normalizedType)) {
            return "ana.conselho";
        }
        return "joao.silva";
    }
}
