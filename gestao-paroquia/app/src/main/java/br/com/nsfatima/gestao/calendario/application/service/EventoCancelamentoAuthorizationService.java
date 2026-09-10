package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.domain.policy.AutorizacaoCancelamentoEventoPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.CancelamentoRequestMode;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço de aplicação que centraliza a autorização para cancelamento de eventos
 * adaptando o {@link EventoActorContext} para a {@link AutorizacaoCancelamentoEventoPolicy}.
 */
@Service
public class EventoCancelamentoAuthorizationService {

    private final AutorizacaoCancelamentoEventoPolicy policy;

    public EventoCancelamentoAuthorizationService(AutorizacaoCancelamentoEventoPolicy policy) {
        this.policy = policy;
    }

    public CancelamentoRequestMode resolveRequestMode(
            EventoActorContext actorContext,
            UUID eventoGrupoParoquialResponsavelId) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoGrupo normalizedOrgType = TipoGrupo.fromStoredValue(actorContext.organizationType());
        return policy.resolveRequestMode(
                normalizedRole, normalizedOrgType, actorContext.organizationId(), eventoGrupoParoquialResponsavelId);
    }

    public void assertCanDecideAprovacao(EventoActorContext actorContext) {
        PapelOrganizacional normalizedRole = PapelOrganizacional.fromStoredValue(actorContext.role());
        TipoGrupo normalizedOrgType = TipoGrupo.fromStoredValue(actorContext.organizationType());
        policy.assertCanDecideAprovacao(normalizedRole, normalizedOrgType);
    }
}
