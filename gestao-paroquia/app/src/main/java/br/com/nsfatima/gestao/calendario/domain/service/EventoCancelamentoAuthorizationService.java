package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Serviço de domínio que decide, a partir do papel e escopo organizacional do
 * ator, se o cancelamento de um evento pode ser aplicado imediatamente ou se
 * precisa passar pelo fluxo de aprovação, e quem tem autoridade para decidir essa
 * aprovação. Pároco e lideranças do Conselho Pastoral cancelam imediatamente;
 * Vigário e lideranças locais (pastorais) precisam de aprovação — estas últimas
 * apenas para eventos da própria organização; os demais papéis não podem cancelar
 * eventos.
 */
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

    /**
     * Determina o modo de cancelamento aplicável ao ator para o evento informado:
     * imediato para Pároco e lideranças do Conselho Pastoral; sujeito a aprovação
     * para Vigário e para lideranças locais restritas à própria organização
     * responsável pelo evento.
     *
     * @param actorContext contexto de papel/organização do ator autenticado
     * @param eventoOrganizacaoResponsavelId identificador da organização responsável pelo evento a ser cancelado
     * @return modo de cancelamento aplicável (imediato ou sujeito a aprovação)
     * @throws ForbiddenOperationException se o ator não tiver escopo de organização, estiver fora do escopo do evento, ou não tiver papel autorizado a cancelar eventos
     */
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

    /**
     * Garante que o ator tem autoridade para decidir (aprovar/reprovar) uma
     * solicitação de aprovação de cancelamento de evento — restrito a Pároco e
     * lideranças do Conselho Pastoral.
     *
     * @param actorContext contexto de papel/organização do ator autenticado
     * @throws ForbiddenOperationException se o ator não tiver papel autorizado a decidir a aprovação
     */
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

    /**
     * Modo de processamento de uma solicitação de cancelamento de evento, definido
     * conforme o papel/escopo do ator solicitante.
     */
    public enum CancelamentoRequestMode {
        /** O cancelamento é aplicado imediatamente, sem passar pelo fluxo de aprovação. */
        IMMEDIATE,
        /** O cancelamento fica pendente até ser decidido através do fluxo de aprovação. */
        REQUIRES_APROVACAO
    }
}
