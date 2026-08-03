package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.strategy;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoCanceladoResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.CancelEventoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Estratégia de execução do fluxo de aprovação para solicitações de cancelamento de
 * evento (tipo {@link TipoSolicitacaoInput#CANCELAMENTO}). Quando um cancelamento é
 * desviado para aprovação (ator sem nível hierárquico suficiente), esta estratégia é
 * quem efetivamente aplica o cancelamento no evento após a decisão de aprovação,
 * reconstruindo o id do evento e o motivo do cancelamento a partir do payload da
 * solicitação (com fallback para o snapshot registrado na própria entidade de aprovação).
 */
@Component
public class CancelEventoExecutionStrategy implements EstrategiaExecucaoAprovacao {

    private final CancelEventoUseCase cancelEventoUseCase;
    private final EventoJpaRepository eventoRepository;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    public CancelEventoExecutionStrategy(
            CancelEventoUseCase cancelEventoUseCase,
            EventoJpaRepository eventoRepository,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper) {
        this.cancelEventoUseCase = cancelEventoUseCase;
        this.eventoRepository = eventoRepository;
        this.aprovacaoAcaoPayloadMapper = aprovacaoAcaoPayloadMapper;
    }

    /**
     * Indica se esta estratégia trata o tipo de solicitação informado.
     *
     * @param tipo tipo da solicitação de aprovação
     * @return {@code true} apenas para {@link TipoSolicitacaoInput#CANCELAMENTO}
     */
    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.CANCELAMENTO;
    }

    /**
     * Aplica o cancelamento do evento após a aprovação da solicitação, resolvendo o
     * evento e o motivo do cancelamento a partir do payload de ação (com fallback para
     * o motivo e o id de evento registrados na própria entidade de aprovação), e
     * registra o id do evento afetado na entidade de aprovação para rastreabilidade.
     *
     * @param aprovacao entidade de aprovação já decidida, contendo o payload da ação
     * @param actorContext contexto do ator que decidiu a aprovação, usado como autor
     *                       do cancelamento efetivo
     * @return resultado da execução com status "EXECUTED", id e status do evento cancelado
     * @throws IllegalArgumentException se não houver id de evento no payload nem na entidade
     */
    @Override
    public AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao,
                                                             EventoActorContext actorContext) {

        AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
        UUID eventoId = payload.eventoId() != null ? payload.eventoId() : aprovacao.getEventoId();

        if (eventoId == null) {
            throw new IllegalArgumentException(
                    "eventoId must be present in approval payload or entity for cancellation");
        }

        String motivo = payload.canceladoMotivo();
        if (motivo == null || motivo.isBlank()) {
            motivo = payload.motivo();
        }
        if (motivo == null || motivo.isBlank()) {
            motivo = aprovacao.getMotivoCancelamentoSnapshot();
        }

        EventoCanceladoResponse response = cancelEventoUseCase.executeApprovedCancellation(
                eventoId,
                motivo,
                actorContext.actor(),
                actorContext.usuarioId());

        aprovacao.setEventoId(response.id());

        return new AprovacaoDecisionResponse.ActionExecution(
                "EXECUTED",
                response.id(),
                response.status(),
                null);
    }

    /**
     * Consulta o status atual do evento associado à solicitação de aprovação, sem
     * adquirir lock de linha, para exibição em telas de acompanhamento da aprovação.
     *
     * @param aprovacao entidade de aprovação cujo evento associado será consultado
     * @return status atual do evento, ou {@code null} se não houver evento associado
     */
    @Override
    public String fetchResourceStatus(AprovacaoEntity aprovacao) {
        UUID eventoId = aprovacao.getEventoId();
        if (eventoId == null) {
            AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
            eventoId = payload.eventoId();
        }

        if (eventoId != null) {
            return eventoRepository.findStatusByIdNoLock(eventoId).orElse(null);
        }
        return null;
    }
}
