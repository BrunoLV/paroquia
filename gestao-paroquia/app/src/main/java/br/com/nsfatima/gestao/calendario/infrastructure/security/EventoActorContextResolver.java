package br.com.nsfatima.gestao.calendario.infrastructure.security;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.iam.infrastructure.security.ExternalMembershipReader;

import br.com.nsfatima.gestao.calendario.domain.policy.AuthorizationPolicy;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import java.util.Locale;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class EventoActorContextResolver {

    private final AuthorizationPolicy authorizationPolicy;

    public EventoActorContextResolver(AuthorizationPolicy authorizationPolicy) {
        this.authorizationPolicy = authorizationPolicy;
    }

    public EventoActorContext resolveRequired() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Authenticated user required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UsuarioDetails usuarioDetails) {
            ExternalMembershipReader.Membership membership = usuarioDetails.primaryMembership()
                    .orElseThrow(() -> new AccessDeniedException(
                            "Authenticated user has no active organizational membership"));

            if (!authorizationPolicy.isRoleAllowed(membership.organizationType(), membership.role())) {
                throw new RoleScopeInvalidException("Role is incompatible with organization type");
            }

            return new EventoActorContext(
                    authentication.getName(),
                    membership.role(),
                    membership.organizationType(),
                    membership.organizationId(),
                    usuarioDetails.getUsuarioId());
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String raw = authority.getAuthority();
            if (raw == null || !raw.startsWith("ROLE_") || "ROLE_USER".equals(raw)) {
                continue;
            }

            String[] parts = raw.substring("ROLE_".length()).split("_", 2);
            if (parts.length != 2) {
                continue;
            }

            String organizationType = parts[0].toUpperCase(Locale.ROOT);
            String role = parts[1].toLowerCase(Locale.ROOT).replace('_', '-');
            if (authorizationPolicy.isRoleAllowed(organizationType, role)) {
                return new EventoActorContext(authentication.getName(), role, organizationType, null, null);
            }
        }

        return new EventoActorContext(
                authentication.getName(),
                PapelOrganizacional.COORDENADOR.storedValue(),
                TipoOrganizacao.PASTORAL.name(),
                null,
                null);
    }
}
