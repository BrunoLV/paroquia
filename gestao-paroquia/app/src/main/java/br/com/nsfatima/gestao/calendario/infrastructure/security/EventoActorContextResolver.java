package br.com.nsfatima.gestao.calendario.infrastructure.security;

import br.com.nsfatima.gestao.calendario.domain.policy.CompatibilidadePapelOrganizacionalPolicy;
import br.com.nsfatima.gestao.iam.application.service.PapelGlobalAuthorizationService;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
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
import java.util.List;
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

    private static final String ORG_ADMIN = "ADMIN";
    private static final String ORG_CLERO = "CLERO";
    private static final String ORG_SECRETARIA = "SECRETARIA";

    private final List<CompatibilidadePapelOrganizacionalPolicy> compatibilidadePolicies;
    private final PapelGlobalAuthorizationService papelGlobalAuthorizationService;

    public EventoActorContextResolver(List<CompatibilidadePapelOrganizacionalPolicy> compatibilidadePolicies,
                                       PapelGlobalAuthorizationService papelGlobalAuthorizationService) {
        this.compatibilidadePolicies = List.copyOf(compatibilidadePolicies);
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
            UUID primaryOrgId = usuarioDetails.vinculoPrimario()
                    .map(LeitorVinculoExterno.Vinculo::organizationId)
                    .orElse(null);
            UUID primaryIgrejaId = usuarioDetails.vinculoPrimario()
                    .map(LeitorVinculoExterno.Vinculo::igrejaVinculoId)
                    .orElse(null);

            boolean isAdmin = usuarioDetails.getAuthorities().stream()
                    .anyMatch(a -> Objects.requireNonNull(a.getAuthority()).equals(ORG_ADMIN)
                            || a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_GLOBAL_ADMIN"));

            if (isAdmin) {
                return new EventoActorContext(
                        authentication.getName(),
                        PapelGlobal.ADMIN.storedValue(),
                        ORG_ADMIN,
                        primaryOrgId,
                        usuarioDetails.getUsuarioId(),
                        primaryIgrejaId);
            }

            boolean isParoco = usuarioDetails.getAuthorities().stream()
                    .anyMatch(a -> Objects.requireNonNull(a.getAuthority()).equals("ROLE_GLOBAL_PAROCO")
                            || a.getAuthority().equals("ROLE_CLERO_PAROCO")
                            || a.getAuthority().equals("PAROCO")
                            || a.getAuthority().equals("ROLE_PAROCO"));

            if (isParoco) {
                return new EventoActorContext(
                        authentication.getName(),
                        PapelGlobal.PAROCO.storedValue(),
                        ORG_CLERO,
                        primaryOrgId,
                        usuarioDetails.getUsuarioId(),
                        primaryIgrejaId);
            }

            boolean isBispo = usuarioDetails.getAuthorities().stream()
                    .anyMatch(a -> Objects.requireNonNull(a.getAuthority()).equals("ROLE_GLOBAL_BISPO_DIOCESANO")
                            || a.getAuthority().equals("ROLE_CLERO_BISPO_DIOCESANO")
                            || a.getAuthority().equals("BISPO_DIOCESANO")
                            || a.getAuthority().equals("ROLE_BISPO_DIOCESANO"));

            if (isBispo) {
                return new EventoActorContext(
                        authentication.getName(),
                        PapelGlobal.BISPO_DIOCESANO.storedValue(),
                        ORG_CLERO,
                        primaryOrgId,
                        usuarioDetails.getUsuarioId(),
                        primaryIgrejaId);
            }

            boolean isVigario = usuarioDetails.getAuthorities().stream()
                    .anyMatch(a -> Objects.requireNonNull(a.getAuthority()).equals("ROLE_GLOBAL_VIGARIO")
                            || a.getAuthority().equals("ROLE_CLERO_VIGARIO")
                            || a.getAuthority().equals("VIGARIO")
                            || a.getAuthority().equals("ROLE_VIGARIO"));

            if (isVigario) {
                return new EventoActorContext(
                        authentication.getName(),
                        PapelGlobal.VIGARIO.storedValue(),
                        ORG_CLERO,
                        primaryOrgId,
                        usuarioDetails.getUsuarioId(),
                        primaryIgrejaId);
            }

            boolean isDiacono = usuarioDetails.getAuthorities().stream()
                    .anyMatch(a -> Objects.requireNonNull(a.getAuthority()).equals("ROLE_GLOBAL_DIACONO_PAROQUIAL")
                            || a.getAuthority().equals("ROLE_CLERO_DIACONO_PAROQUIAL")
                            || a.getAuthority().equals("DIACONO_PAROQUIAL")
                            || a.getAuthority().equals("ROLE_DIACONO_PAROQUIAL"));

            if (isDiacono) {
                return new EventoActorContext(
                        authentication.getName(),
                        PapelGlobal.DIACONO_PAROQUIAL.storedValue(),
                        ORG_CLERO,
                        primaryOrgId,
                        usuarioDetails.getUsuarioId(),
                        primaryIgrejaId);
            }

            boolean isSecretaria = usuarioDetails.getAuthorities().stream()
                    .anyMatch(a -> Objects.requireNonNull(a.getAuthority()).equals("ROLE_GLOBAL_SECRETARIA_PAROQUIAL")
                            || a.getAuthority().equals("SECRETARIA_PAROQUIAL")
                            || a.getAuthority().equals("ROLE_SECRETARIA_PAROQUIAL"));

            if (isSecretaria) {
                return new EventoActorContext(
                        authentication.getName(),
                        PapelGlobal.SECRETARIA_PAROQUIAL.storedValue(),
                        ORG_SECRETARIA,
                        primaryOrgId,
                        usuarioDetails.getUsuarioId(),
                        primaryIgrejaId);
            }

            boolean isFiel = usuarioDetails.getAuthorities().stream()
                    .anyMatch(a -> Objects.requireNonNull(a.getAuthority()).equals("ROLE_GLOBAL_FIEL")
                            || a.getAuthority().equals("FIEL")
                            || a.getAuthority().equals("ROLE_FIEL"));

            boolean hasValidOrganizationalVinculo = usuarioDetails.getVinculos().stream()
                    .anyMatch(m -> aceitaPapel(m.organizationType(), m.role()));

            if (isFiel && !hasValidOrganizationalVinculo) {
                return new EventoActorContext(
                        authentication.getName(),
                        PapelGlobal.FIEL.storedValue(),
                        "PAROQUIA",
                        primaryOrgId,
                        usuarioDetails.getUsuarioId(),
                        primaryIgrejaId);
            }

            LeitorVinculoExterno.Vinculo vinculo = usuarioDetails.getVinculos().stream()
                    .filter(m -> aceitaPapel(m.organizationType(), m.role()))
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

            String stripped = raw.substring("ROLE_".length());
            PapelGlobal directGlobal = PapelGlobal.fromStoredValue(stripped);
            if (directGlobal != null) {
                String defaultOrgType = resolveOrgTypeForPapelGlobal(directGlobal);
                return new EventoActorContext(
                        authentication.getName(), directGlobal.storedValue(), defaultOrgType, null, null, null);
            }

            PapelOrganizacional directPapel = PapelOrganizacional.fromStoredValue(stripped);
            if (directPapel != null) {
                String defaultOrgType = switch (directPapel) {
                    case SECRETARIO_CONSELHO,
                         MEMBRO_CONSELHO_PASTORAL,
                         MEMBRO_CONSELHO_ECONOMICO -> TipoGrupo.CONSELHO.name();
                    default -> TipoGrupo.PASTORAL.name();
                };
                return new EventoActorContext(
                        authentication.getName(), directPapel.storedValue(), defaultOrgType, null, null, null);
            }

            String[] parts = stripped.split("_", 2);
            if (parts.length != 2) {
                continue;
            }

            PapelGlobal globalPapel = PapelGlobal.fromStoredValue(parts[1]);
            if (globalPapel != null) {
                String orgType = "GLOBAL".equalsIgnoreCase(parts[0])
                        ? resolveOrgTypeForPapelGlobal(globalPapel)
                        : parts[0].toUpperCase(Locale.ROOT);
                return new EventoActorContext(
                        authentication.getName(), globalPapel.storedValue(), orgType, null, null, null);
            }

            String organizationType = parts[0].toUpperCase(Locale.ROOT);
            String role = parts[1].toLowerCase(Locale.ROOT).replace('_', '-');
            if (aceitaPapel(organizationType, role)) {
                return new EventoActorContext(
                        authentication.getName(), role, organizationType, null, null, null);
            }
        }

        return new EventoActorContext(
                authentication.getName(),
                PapelOrganizacional.COORDENADOR.storedValue(),
                TipoGrupo.PASTORAL.name(),
                null,
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                null);
    }

    private boolean aceitaPapel(String tipoOrganizacao, String papelBruto) {
        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(papelBruto);
        if (papel == null) {
            return false;
        }
        TipoGrupo tipoGrupo;
        try {
            tipoGrupo = TipoGrupo.fromStoredValue(tipoOrganizacao);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        String tipoNormalizado = tipoGrupo == null ? tipoOrganizacao : tipoGrupo.name();
        return compatibilidadePolicies.stream()
                .anyMatch(policy -> policy.aceita(tipoNormalizado, papel));
    }

    private String resolveOrgTypeForPapelGlobal(PapelGlobal papelGlobal) {
        return switch (papelGlobal) {
            case PAROCO, VIGARIO, DIACONO_PAROQUIAL, BISPO_DIOCESANO -> ORG_CLERO;
            case ADMIN -> ORG_ADMIN;
            case SECRETARIA_PAROQUIAL -> ORG_SECRETARIA;
            default -> TipoGrupo.PASTORAL.name();
        };
    }
}
