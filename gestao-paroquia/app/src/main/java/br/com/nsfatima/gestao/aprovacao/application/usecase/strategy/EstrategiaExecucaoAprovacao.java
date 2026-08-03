package br.com.nsfatima.gestao.aprovacao.application.usecase.strategy;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;

public interface EstrategiaExecucaoAprovacao {

    boolean supports(TipoSolicitacaoInput tipo);

    AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao, EventoActorContext actorContext);

    String fetchResourceStatus(AprovacaoEntity aprovacao);

    default void onRejected(AprovacaoEntity aprovacao) {
        // No-op by default; strategies whose pending resource needs cleanup on rejection may override this.
    }
}
