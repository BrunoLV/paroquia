package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class EventoPatchAuthorizationService {

    private static final Set<PapelOrganizacional> ORG_EDITOR_ROLES = Set.of(
            PapelOrganizacional.COORDENADOR,
            PapelOrganizacional.VICE_COORDENADOR);
    private static final Set<PapelOrganizacional> ORG_CREATOR_ROLES = Set.of(
            PapelOrganizacional.COORDENADOR,
            PapelOrganizacional.VICE_COORDENADOR,
            PapelOrganizacional.SECRETARIO,
            PapelOrganizacional.PAROCO,
            PapelOrganizacional.VIGARIO,
            PapelOrganizacional.PADRE);

    public void assertCanCreate(EventoActorContext actorContext, UUID organizacaoResponsavelId) {
        resolveCreateRequestMode(actorContext, organizacaoResponsavelId);
    }

    public CreateRequestMode resolveCreateRequestMode(EventoActorContext actorContext, UUID organizacaoResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());

        if (!ORG_CREATOR_ROLES.contains(normalizedRole)) {
            throw new AccessDeniedException("User does not have permission for this create operation");
        }

        if (actorContext.organizationId() != null
                && organizacaoResponsavelId != null
                && !organizacaoResponsavelId.equals(actorContext.organizationId())
                && normalizedRole != PapelOrganizacional.PAROCO) {
            throw new AccessDeniedException("User cannot create events outside the responsible organization scope");
        }

        if (normalizedRole == PapelOrganizacional.PAROCO
                || (normalizedOrgType == TipoOrganizacao.CONSELHO
                        && (normalizedRole == PapelOrganizacional.COORDENADOR
                                || normalizedRole == PapelOrganizacional.VICE_COORDENADOR))) {
            return CreateRequestMode.IMMEDIATE;
        }

        return CreateRequestMode.REQUIRES_APPROVAL;
    }

    public void assertCanEditGeneral(EventoActorContext actorContext, UUID eventoOrganizacaoResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        if (!ORG_EDITOR_ROLES.contains(normalizedRole)) {
            throw new ForbiddenOperationException("User does not have permission for this patch operation");
        }
        if (actorContext.organizationId() == null) {
            return;
        }
        if (!eventoOrganizacaoResponsavelId.equals(actorContext.organizationId())) {
            throw new ForbiddenOperationException("User cannot edit events outside the responsible organization scope");
        }
    }

    public void assertCanManageParticipants(EventoActorContext actorContext, UUID eventoOrganizacaoResponsavelId) {
        assertCanEditGeneral(actorContext, eventoOrganizacaoResponsavelId);
    }

    public void assertCanChangeResponsibleOrganization(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());
        boolean conselhoCoordinator = normalizedOrgType == TipoOrganizacao.CONSELHO
                && (normalizedRole == PapelOrganizacional.COORDENADOR
                        || normalizedRole == PapelOrganizacional.VICE_COORDENADOR);
        boolean parroco = normalizedRole == PapelOrganizacional.PAROCO;

        if (!conselhoCoordinator && !parroco) {
            throw new ForbiddenOperationException(
                    "Only conselho coordinator or parroco can change responsible organization");
        }
    }

    public enum CreateRequestMode {
        IMMEDIATE,
        REQUIRES_APPROVAL
    }
}
