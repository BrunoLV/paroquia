package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class EventoPatchAuthorizationService {

    private static final Set<PapelOrganizacional> ORG_EDITOR_ROLES = Set.of(
            PapelOrganizacional.ADMIN,
            PapelOrganizacional.COORDENADOR_PASTORAL_GLOBAL,
            PapelOrganizacional.COORDENADOR_PASTORAL_LOCAL,
            PapelOrganizacional.COORDENADOR_MOVIMENTO,
            PapelOrganizacional.COORDENADOR_PASTORAL,
            PapelOrganizacional.PRESIDENTE_CPC,
            PapelOrganizacional.MESA_DIRETORA_CPP,
            PapelOrganizacional.PAROCO,
            PapelOrganizacional.VIGARIO,
            PapelOrganizacional.PADRE);
    private static final Set<PapelOrganizacional> ORG_CREATOR_ROLES = Set.of(
            PapelOrganizacional.ADMIN,
            PapelOrganizacional.COORDENADOR_PASTORAL_GLOBAL,
            PapelOrganizacional.COORDENADOR_PASTORAL_LOCAL,
            PapelOrganizacional.COORDENADOR_MOVIMENTO,
            PapelOrganizacional.COORDENADOR_PASTORAL,
            PapelOrganizacional.PRESIDENTE_CPC,
            PapelOrganizacional.MESA_DIRETORA_CPP,
            PapelOrganizacional.SECRETARIO,
            PapelOrganizacional.PAROCO,
            PapelOrganizacional.VIGARIO,
            PapelOrganizacional.PADRE);

    public void assertCanCreate(EventoActorContext actorContext, UUID organizacaoResponsavelId, UUID igrejaVinculoId) {
        resolveCreateRequestMode(actorContext, organizacaoResponsavelId, igrejaVinculoId);
    }

    public CreateRequestMode resolveCreateRequestMode(EventoActorContext actorContext,
                                                      UUID organizacaoResponsavelId, UUID igrejaVinculoId) {

        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());

        if (!ORG_CREATOR_ROLES.contains(normalizedRole)) {
            throw new AccessDeniedException("User does not have permission for this create operation");
        }

        if (normalizedRole != PapelOrganizacional.PAROCO && actorContext.organizationId() != null) {
            boolean matchesOrg = actorContext.organizationId().equals(organizacaoResponsavelId);
            boolean matchesChurch = (igrejaVinculoId == null) ||
                    (actorContext.igrejaVinculoId() != null && actorContext.igrejaVinculoId().equals(igrejaVinculoId));

            if (!matchesOrg || !matchesChurch) {
                boolean hasOtherValidMembership = checkUserMembership(organizacaoResponsavelId,
                        igrejaVinculoId, ORG_CREATOR_ROLES);

                if (!hasOtherValidMembership) {
                    throw new AccessDeniedException(
                            "User cannot create events outside the responsible organization scope");
                }
            }
        }

        if (normalizedRole == PapelOrganizacional.PAROCO
                || ((normalizedOrgType == TipoOrganizacao.CONSELHO_PASTORAL
                || normalizedOrgType == TipoOrganizacao.CPP
                || normalizedOrgType == TipoOrganizacao.CPC)
                && (normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                || normalizedRole == PapelOrganizacional.MESA_DIRETORA_CPP
                || normalizedRole == PapelOrganizacional.COORDENADOR_PASTORAL_GLOBAL))) {
            return CreateRequestMode.IMMEDIATE;
        }

        return CreateRequestMode.REQUIRES_APPROVAL;
    }

    public void assertCanEditGeneral(EventoActorContext actorContext,
                                     UUID eventoOrganizacaoResponsavelId, UUID eventIgrejaVinculoId) {

        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        if (!ORG_EDITOR_ROLES.contains(normalizedRole)) {
            throw new ForbiddenOperationException("User does not have permission for this patch operation");
        }

        if (normalizedRole != PapelOrganizacional.PAROCO && actorContext.organizationId() != null) {
            boolean matchesOrg = actorContext.organizationId().equals(eventoOrganizacaoResponsavelId);
            boolean matchesChurch = (eventIgrejaVinculoId == null) || (actorContext.igrejaVinculoId() != null &&
                    actorContext.igrejaVinculoId().equals(eventIgrejaVinculoId));

            if (!matchesOrg || !matchesChurch) {
                boolean hasOtherValidMembership = checkUserMembership(eventoOrganizacaoResponsavelId,
                        eventIgrejaVinculoId, ORG_EDITOR_ROLES);

                if (!hasOtherValidMembership) {
                    throw new ForbiddenOperationException(
                            "User cannot edit events outside the responsible organization scope");
                }
            }
        }
    }

    public void assertCanManageParticipants(EventoActorContext actorContext,
                                            UUID eventoOrganizacaoResponsavelId, UUID eventIgrejaVinculoId) {

        assertCanEditGeneral(actorContext, eventoOrganizacaoResponsavelId, eventIgrejaVinculoId);
    }

    public void assertCanChangeResponsibleOrganization(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());
        boolean conselhoCoordinator = (normalizedOrgType == TipoOrganizacao.CONSELHO_PASTORAL
                || normalizedOrgType == TipoOrganizacao.CPP
                || normalizedOrgType == TipoOrganizacao.CPC)
                && (normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                || normalizedRole == PapelOrganizacional.MESA_DIRETORA_CPP
                || normalizedRole == PapelOrganizacional.COORDENADOR_PASTORAL_GLOBAL);
        boolean parroco = normalizedRole == PapelOrganizacional.PAROCO;

        if (!conselhoCoordinator && !parroco) {
            throw new ForbiddenOperationException(
                    "Only conselho coordinator or parroco can change responsible organization");
        }
    }

    private boolean checkUserMembership(UUID organizacaoResponsavelId,
                                        UUID eventIgrejaVinculoId, Set<PapelOrganizacional> allowedRoles) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        if (auth.getPrincipal() instanceof UsuarioDetails usuarioDetails) {
            return usuarioDetails.getMemberships().stream()
                    .anyMatch(m -> {
                        boolean sameOrg = m.organizationId().equals(organizacaoResponsavelId);
                        boolean roleAllowed = allowedRoles.contains(PapelOrganizacional.fromStoredValue(m.role()));
                        boolean sameChurch = (eventIgrejaVinculoId == null) ||
                                (m.igrejaVinculoId() != null && m.igrejaVinculoId().equals(eventIgrejaVinculoId));
                        return sameOrg && roleAllowed && sameChurch;
                    });
        }
        return false;
    }

    public enum CreateRequestMode {
        IMMEDIATE,
        REQUIRES_APPROVAL
    }
}
