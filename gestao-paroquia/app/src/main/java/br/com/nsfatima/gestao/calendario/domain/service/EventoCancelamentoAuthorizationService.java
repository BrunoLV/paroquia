package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class EventoCancelamentoAuthorizationService {

    private static final Set<PapelOrganizacional> LEADERSHIP_ROLES = Set.of(
            PapelOrganizacional.COORD_PASTORAL_GLOBAL,
            PapelOrganizacional.COORD_PASTORAL_LOCAL,
            PapelOrganizacional.COORD_MOVIMENTO,
            PapelOrganizacional.PRESIDENTE_CPC,
            PapelOrganizacional.MESA_CPP,
            PapelOrganizacional.COORD_PASTORAL);
    private static final Set<TipoOrganizacao> LOCAL_SCOPED_ORG_TYPES = Set.of(
            TipoOrganizacao.PASTORAL);

    public CancelamentoRequestMode resolveRequestMode(
            EventoActorContext actorContext,
            UUID eventoOrganizacaoResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());

        if (isConselhoLeadership(normalizedRole, normalizedOrgType) || normalizedRole == PapelOrganizacional.PAROCO) {
            return CancelamentoRequestMode.IMMEDIATE;
        }

        if (normalizedRole == PapelOrganizacional.VIGARIO) {
            return CancelamentoRequestMode.REQUIRES_APROVACAO;
        }

        if (isLocalLeadership(normalizedRole, normalizedOrgType)) {
            UUID actorOrganizationId = actorContext.organizationId();
            if (actorOrganizationId == null) {
                throw new ForbiddenOperationException("Usuário não pode cancelar eventos sem um escopo de organização");
            }
            if (eventoOrganizacaoResponsavelId == null || !eventoOrganizacaoResponsavelId.equals(actorOrganizationId)) {
                throw new ForbiddenOperationException(
                        "User cannot cancel events outside the responsible organization scope");
            }
            return CancelamentoRequestMode.REQUIRES_APROVACAO;
        }

        throw new ForbiddenOperationException("Usuário não tem permissão para cancelar eventos");
    }

    public void assertCanDecideAprovacao(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoOrganizacao normalizedOrgType = TipoOrganizacao.fromStoredValue(actorContext.organizationType());
        if (normalizedRole == PapelOrganizacional.PAROCO || isConselhoLeadership(normalizedRole, normalizedOrgType)) {
            return;
        }
        throw new ForbiddenOperationException("Usuário não tem permissão para decidir aprovações de evento");
    }

    private boolean isConselhoLeadership(PapelOrganizacional role, TipoOrganizacao organizationType) {
        return organizationType == TipoOrganizacao.CONSELHO_PASTORAL && LEADERSHIP_ROLES.contains(role);
    }

    private boolean isLocalLeadership(PapelOrganizacional role, TipoOrganizacao organizationType) {
        return LOCAL_SCOPED_ORG_TYPES.contains(organizationType) && LEADERSHIP_ROLES.contains(role);
    }

    public enum CancelamentoRequestMode {
        IMMEDIATE,
        REQUIRES_APROVACAO
    }
}
