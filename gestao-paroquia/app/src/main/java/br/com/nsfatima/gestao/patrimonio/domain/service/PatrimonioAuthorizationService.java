package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class PatrimonioAuthorizationService {

    private static final Set<PapelOrganizacional> PATRIMONIO_EDITOR_ROLES = Set.of(
            PapelOrganizacional.ADMIN,
            PapelOrganizacional.PAROCO,
            PapelOrganizacional.COORD_PASTORAL_GLOBAL,
            PapelOrganizacional.COORD_PASTORAL_LOCAL,
            PapelOrganizacional.COORD_MOVIMENTO,
            PapelOrganizacional.COORD_PASTORAL,
            PapelOrganizacional.PRESIDENTE_CPC,
            PapelOrganizacional.MESA_CPP);

    public void assertCanCreate(EventoActorContext actorContext, UUID organizacaoId) {
        assertScoped(actorContext, organizacaoId, "User does not have permission to create assets",
                "User cannot create assets for another organization");
    }

    public void assertCanTransfer(EventoActorContext actorContext, UUID ativoOrganizacaoId) {
        assertScoped(actorContext, ativoOrganizacaoId, "User does not have permission to transfer assets",
                "User cannot transfer assets of another organization");
    }

    public void assertCanAlocar(EventoActorContext actorContext, UUID ativoOrganizacaoId) {
        assertScoped(actorContext, ativoOrganizacaoId, "User does not have permission to allocate assets",
                "User cannot allocate assets of another organization");
    }

    public void assertCanDesalocar(EventoActorContext actorContext, UUID ativoOrganizacaoId) {
        assertScoped(actorContext, ativoOrganizacaoId, "User does not have permission to deallocate assets",
                "User cannot deallocate assets of another organization");
    }

    public void assertCanIniciarManutencao(EventoActorContext actorContext, UUID ativoOrganizacaoId) {
        assertScoped(actorContext, ativoOrganizacaoId, "User does not have permission to put assets into maintenance",
                "User cannot put assets of another organization into maintenance");
    }

    private void assertScoped(EventoActorContext actorContext, UUID organizacaoId, String roleMessage,
                               String scopeMessage) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());

        if (!PATRIMONIO_EDITOR_ROLES.contains(normalizedRole)) {
            throw new ForbiddenOperationException(roleMessage);
        }

        if (!isHighLevelActor(actorContext) && actorContext.organizationId() != null
                && !actorContext.organizationId().equals(organizacaoId)) {
            throw new ForbiddenOperationException(scopeMessage);
        }
    }

    public boolean isHighLevelActor(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        return normalizedRole == PapelOrganizacional.ADMIN
                || normalizedRole == PapelOrganizacional.PAROCO
                || normalizedRole == PapelOrganizacional.PRESIDENTE_CPC
                || normalizedRole == PapelOrganizacional.MESA_CPP
                || normalizedRole == PapelOrganizacional.COORD_PASTORAL_GLOBAL;
    }
}
