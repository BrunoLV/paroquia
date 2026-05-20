package br.com.nsfatima.gestao.projeto.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ProjetoAuthorizationService {

    private static final Set<PapelOrganizacional> PROJECT_EDITOR_ROLES = Set.of(
            PapelOrganizacional.COORDENADOR,
            PapelOrganizacional.VICE_COORDENADOR,
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
        boolean isHighLevelActor = normalizedRole == PapelOrganizacional.PAROCO 
                || (normalizedOrgType == TipoOrganizacao.CONSELHO && (normalizedRole == PapelOrganizacional.COORDENADOR || normalizedRole == PapelOrganizacional.VICE_COORDENADOR));

        if (!isHighLevelActor && actorContext.organizationId() != null && !actorContext.organizationId().equals(organizacaoResponsavelId)) {
            throw new AccessDeniedException("User cannot create projects for another organization");
        }
    }

    public void assertCanEdit(EventoActorContext actorContext, UUID projectOrganizacaoResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());

        if (!PROJECT_EDITOR_ROLES.contains(normalizedRole)) {
            throw new ForbiddenOperationException("User does not have permission to edit projects");
        }

        boolean isHighLevelActor = normalizedRole == PapelOrganizacional.PAROCO 
                || (normalizedOrgType == TipoOrganizacao.CONSELHO && (normalizedRole == PapelOrganizacional.COORDENADOR || normalizedRole == PapelOrganizacional.VICE_COORDENADOR));

        if (!isHighLevelActor && actorContext.organizationId() != null && !actorContext.organizationId().equals(projectOrganizacaoResponsavelId)) {
            throw new ForbiddenOperationException("User cannot edit projects of another organization");
        }
    }
}
