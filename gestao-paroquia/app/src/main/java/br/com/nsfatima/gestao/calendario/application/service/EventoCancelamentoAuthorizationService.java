package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.domain.policy.AutorizacaoCancelamentoEventoPolicy;
import br.com.nsfatima.gestao.calendario.domain.policy.ModoRequisicaoCancelamento;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
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

    public ModoRequisicaoCancelamento resolveRequestMode(
            EventoActorContext actorContext,
            UUID eventoGrupoParoquialResponsavelId) {
        return policy.resolveRequestMode(
                actorContext.role(),
                actorContext.organizationType(),
                actorContext.organizationId(),
                eventoGrupoParoquialResponsavelId);
    }

    public void assertCanDecideAprovacao(EventoActorContext actorContext) {
        policy.assertCanDecideAprovacao(actorContext.role(), actorContext.organizationType());
    }
}
