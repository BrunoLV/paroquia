package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EventoCancelamentoAuthorizationService {

    private static final Set<PapelOrganizacional> LEADERSHIP_ROLES = Set.of(
            PapelOrganizacional.COORDENADOR,
            PapelOrganizacional.VICE_COORDENADOR);
    private static final Set<TipoOrganizacao> LOCAL_SCOPED_ORG_TYPES = Set.of(
            TipoOrganizacao.PASTORAL,
            TipoOrganizacao.LAICATO);

    public CancelamentoRequestMode resolveRequestMode(
            EventoActorContext actorContext,
            UUID eventoOrganizacaoResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());

        if (isConselhoLeadership(normalizedRole, normalizedOrgType) || normalizedRole == PapelOrganizacional.PAROCO) {
            return CancelamentoRequestMode.IMMEDIATE;
        }

        if (normalizedRole == PapelOrganizacional.VIGARIO) {
            return CancelamentoRequestMode.REQUIRES_APPROVAL;
        }

        if (isLocalLeadership(normalizedRole, normalizedOrgType)) {
            UUID actorOrganizationId = actorContext.organizationId();
            if (actorOrganizationId == null) {
                throw new ForbiddenOperationException("User cannot cancel events without an organization scope");
            }
            if (eventoOrganizacaoResponsavelId == null || !eventoOrganizacaoResponsavelId.equals(actorOrganizationId)) {
                throw new ForbiddenOperationException(
                        "User cannot cancel events outside the responsible organization scope");
            }
            return CancelamentoRequestMode.REQUIRES_APPROVAL;
        }

        throw new ForbiddenOperationException("User does not have permission to cancel events");
    }

    public void assertCanDecideApproval(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());
        if (normalizedRole == PapelOrganizacional.PAROCO || isConselhoLeadership(normalizedRole, normalizedOrgType)) {
            return;
        }
        throw new ForbiddenOperationException("User does not have permission to decide event approvals");
    }

    private boolean isConselhoLeadership(PapelOrganizacional role, TipoOrganizacao organizationType) {
        return organizationType == TipoOrganizacao.CONSELHO && LEADERSHIP_ROLES.contains(role);
    }

    private boolean isLocalLeadership(PapelOrganizacional role, TipoOrganizacao organizationType) {
        return LOCAL_SCOPED_ORG_TYPES.contains(organizationType) && LEADERSHIP_ROLES.contains(role);
    }

    public enum CancelamentoRequestMode {
        IMMEDIATE,
        REQUIRES_APPROVAL
    }
}
