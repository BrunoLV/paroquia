package br.com.nsfatima.gestao.calendario.infrastructure.security;

import br.com.nsfatima.gestao.calendario.domain.policy.AuthorizationPolicy;
import br.com.nsfatima.gestao.iam.domain.service.PapelGlobalAuthorizationService;
import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Deriva o {@link EventoActorContext} do ator autenticado atual a partir do
 * {@code SecurityContextHolder}, centralizando em um único ponto a lógica de "quem está agindo, com
 * qual papel organizacional e em qual escopo de organização", para que casos de uso e serviços de
 * autorização do contexto de calendário não precisem repetir essa derivação a partir da
 * {@code Authentication} bruta.
 */
@Component
public class EventoActorContextResolver {

    private final AuthorizationPolicy authorizationPolicy;
    private final PapelGlobalAuthorizationService papelGlobalAuthorizationService;

    public EventoActorContextResolver(AuthorizationPolicy authorizationPolicy,
                                       PapelGlobalAuthorizationService papelGlobalAuthorizationService) {
        this.authorizationPolicy = authorizationPolicy;
        this.papelGlobalAuthorizationService = papelGlobalAuthorizationService;
    }

    /**
     * Resolve o contexto do ator autenticado atual, exigindo uma autenticação válida.
     * <p>
     * A resolução segue, em ordem: (1) se o principal for {@code UsuarioDetails} com papel de
     * administrador, retorna um contexto de escopo global (organização {@code null}, papel
     * {@code PAROCO} no tipo {@code CLERO}); (2) se for {@code UsuarioDetails} sem papel de
     * administrador, procura entre seus vínculos organizacionais o primeiro cujo papel seja
     * permitido pela {@link AuthorizationPolicy} para o tipo de organização do vínculo; (3) caso o
     * principal não seja {@code UsuarioDetails}, tenta inferir papel e tipo de organização a partir
     * das {@code GrantedAuthority} no formato {@code ROLE_<TIPO>_<PAPEL>}; (4) por fim, como
     * fallback, assume um papel padrão de coordenador pastoral local.
     *
     * @return o contexto do ator resolvido (ator, papel, tipo e id de organização, usuário e vínculo)
     * @throws org.springframework.security.access.AccessDeniedException se não houver autenticação,
     *     se a autenticação não estiver autenticada, ou se for uma autenticação anônima
     * @throws PapelEscopoInvalidoException se o único papel do usuário não for compatível com o tipo
     *     de organização do seu vínculo
     */
    public EventoActorContext resolveRequired() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Authenticated user required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UsuarioDetails usuarioDetails) {
            boolean isAdmin = usuarioDetails.getAuthorities().stream()
                    .anyMatch(a -> Objects.requireNonNull(a.getAuthority()).equals("ADMIN") ||
                            a.getAuthority().equals("ROLE_ADMIN"))
                    || papelGlobalAuthorizationService.isGlobalByUsuarioId(usuarioDetails.getUsuarioId());

            if (isAdmin) {
                // Contexto sintético de escopo global — "CLERO" aqui é só um sentinel informativo (não é
                // mais um TipoGrupo válido desde a track linguagem_ubiqua_dominio); o papel PAROCO já basta
                // para todo bypass de autorização baseado em papel em EventoPatchAuthorizationService etc.
                return new EventoActorContext(
                        authentication.getName(),
                        PapelOrganizacional.PAROCO.storedValue(),
                        "CLERO",
                        null,
                        usuarioDetails.getUsuarioId(),
                        null);
            }

            LeitorVinculoExterno.Vinculo vinculo = usuarioDetails.getVinculos().stream()
                    .filter(m -> authorizationPolicy.isRoleAllowed(m.organizationType(), m.role()))
                    .findFirst()
                    .orElseThrow(() -> new PapelEscopoInvalidoException(
                            "Papel incompatível com o tipo de organização"));

            return new EventoActorContext(
                    authentication.getName(),
                    vinculo.role(),
                    vinculo.organizationType(),
                    vinculo.organizationId(),
                    usuarioDetails.getUsuarioId(),
                    vinculo.igrejaVinculoId());
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
                return new EventoActorContext(authentication.getName(), role, organizationType, null, null, null);
            }
        }

        return new EventoActorContext(
                authentication.getName(),
                PapelOrganizacional.COORD_PASTORAL_LOCAL.storedValue(),
                TipoGrupo.PASTORAL.name(),
                null,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                null);
    }
}
