package br.com.nsfatima.gestao.patrimonio.application.service;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.patrimonio.domain.policy.AutorizacaoPatrimonioPolicy;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço de aplicação que centraliza a autorização de patrimônio adaptando
 * o {@link EventoActorContext} para a {@link AutorizacaoPatrimonioPolicy}.
 */
@Service
public class PatrimonioAuthorizationService {

    private final AutorizacaoPatrimonioPolicy policy;

    public PatrimonioAuthorizationService(AutorizacaoPatrimonioPolicy policy) {
        this.policy = policy;
    }

    /**
     * Valida se o ator pode gerir o patrimônio da unidade indicada.
     *
     * @param actorContext contexto do ator autenticado
     * @param grupoParoquialId ID da unidade territorial
     */
    public void assertPodeGerirPatrimonio(EventoActorContext actorContext, UUID grupoParoquialId) {
        policy.assertPodeGerirPatrimonio(actorContext.role(), actorContext.organizationId(), grupoParoquialId);
    }

    /**
     * Indica se o ator é considerado de alto nível em patrimônio.
     *
     * @param actorContext contexto do ator autenticado
     * @return true se for alto nível
     */
    public boolean isAtorDeAltoNivel(EventoActorContext actorContext) {
        return policy.isAtorDeAltoNivel(actorContext.role());
    }
}
