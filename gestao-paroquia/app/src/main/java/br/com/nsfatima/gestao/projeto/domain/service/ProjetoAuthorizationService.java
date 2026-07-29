package br.com.nsfatima.gestao.projeto.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class ProjetoAuthorizationService {

    private static final Set<PapelOrganizacional> PROJECT_EDITOR_ROLES = Set.of(
            PapelOrganizacional.ADMIN,
            PapelOrganizacional.COORD_PASTORAL_GLOBAL,
            PapelOrganizacional.COORD_PASTORAL_LOCAL,
            PapelOrganizacional.COORD_MOVIMENTO,
            PapelOrganizacional.COORD_PASTORAL,
            PapelOrganizacional.PRESIDENTE_CPC,
            PapelOrganizacional.MESA_CPP,
            PapelOrganizacional.SECRETARIO,
            PapelOrganizacional.PAROCO,
            PapelOrganizacional.VIGARIO,
            PapelOrganizacional.PADRE);

    public void assertCanCreate(EventoActorContext actorContext, UUID organizacaoResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());

        if (!PROJECT_EDITOR_ROLES.contains(normalizedRole)) {
            throw new AccessDeniedException("User does not have permission to create projects");
        }

        // Only Paroco or Conselho Coordinators can create projects for any organization.
        // Others can only create for their own organization.
        boolean isHighLevelActor = isHighLevelActor(actorContext);

        if (!isHighLevelActor && actorContext.organizationId() != null
                && !actorContext.organizationId().equals(organizacaoResponsavelId)) {
            throw new AccessDeniedException("User cannot create projects for another organization");
        }
    }

    public void assertCanEdit(EventoActorContext actorContext, UUID projectOrganizacaoResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());

        if (!PROJECT_EDITOR_ROLES.contains(normalizedRole)) {
            throw new ForbiddenOperationException("User does not have permission to edit projects");
        }

        boolean isHighLevelActor = isHighLevelActor(actorContext);

        if (!isHighLevelActor && actorContext.organizationId() != null
                && !actorContext.organizationId().equals(projectOrganizacaoResponsavelId)) {
            throw new ForbiddenOperationException("User cannot edit projects of another organization");
        }
    }

    public boolean isHighLevelActor(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());
        return normalizedRole == PapelOrganizacional.PAROCO
                || normalizedRole == PapelOrganizacional.ADMIN
                || ((normalizedOrgType == TipoOrganizacao.CONSELHO_PASTORAL || normalizedOrgType == TipoOrganizacao.CPP || normalizedOrgType == TipoOrganizacao.CPC)
                && (normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                || normalizedRole == PapelOrganizacional.MESA_CPP
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL));
    }

    public void assertCanStartOrConclude(EventoActorContext actorContext, UUID projectOrganizacaoResponsavelId) {
        if (isHighLevelActor(actorContext)) {
            return;
        }
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        boolean isOrgLeader = (normalizedRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL_LOCAL
                || normalizedRole == PapelOrganizacional.COORD_MOVIMENTO
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL
                || normalizedRole == PapelOrganizacional.PRESIDENTE_CPC);
        if (isOrgLeader && actorContext.organizationId() != null
                && actorContext.organizationId().equals(projectOrganizacaoResponsavelId)) {
            return;
        }
        throw new ForbiddenOperationException("User does not have permission to perform this project transition");
    }

    public void assertCanCancel(EventoActorContext actorContext, UUID projectOrganizacaoResponsavelId) {
        assertCanStartOrConclude(actorContext, projectOrganizacaoResponsavelId);
    }
}

