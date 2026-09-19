package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.strategy;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.DecisaoAprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.AtualizarEventoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Estratégia de execução do fluxo de aprovação para solicitações de edição de evento
 * (tipo {@link TipoSolicitacaoInput#EDICAO_EVENTO}). Quando uma atualização é desviada
 * para aprovação (ator sem nível hierárquico suficiente), esta estratégia reconstrói o
 * pedido de atualização a partir do payload salvo na solicitação e aplica a alteração
 * efetiva no evento após a decisão de aprovação.
 */
@Component
public class AtualizarEventoExecutionStrategy implements EstrategiaExecucaoAprovacao {

    private final AtualizarEventoUseCase atualizarEventoUseCase;
    private final EventoJpaRepository eventoRepository;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    public AtualizarEventoExecutionStrategy(
            AtualizarEventoUseCase atualizarEventoUseCase,
            EventoJpaRepository eventoRepository,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper) {
        this.atualizarEventoUseCase = atualizarEventoUseCase;
        this.eventoRepository = eventoRepository;
        this.aprovacaoAcaoPayloadMapper = aprovacaoAcaoPayloadMapper;
    }

    /**
     * Indica se esta estratégia trata o tipo de solicitação informado.
     *
     * @param tipo tipo da solicitação de aprovação
     * @return {@code true} apenas para {@link TipoSolicitacaoInput#EDICAO_EVENTO}
     */
    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.EDICAO_EVENTO;
    }

    /**
     * Aplica a atualização do evento após a aprovação da solicitação, restaurando o
     * pedido de atualização original a partir do payload da ação de aprovação e
     * registrando o id do evento afetado na entidade de aprovação para rastreabilidade.
     *
     * @param aprovacao entidade de aprovação já decidida, contendo o payload da ação
     * @param actorContext contexto do ator que decidiu a aprovação (não utilizado
     *                       diretamente na atualização, mantido pela assinatura comum
     *                       da estratégia de execução)
     * @return resultado da execução com status "EXECUTED", id e status do evento atualizado
     * @throws IllegalArgumentException se não houver id de evento no payload nem na entidade
     */
    @Override
    public DecisaoAprovacaoResponse.ActionExecution execute(AprovacaoEntity aprovacao,
                                                             EventoActorContext actorContext) {

        AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
        UUID eventoId = payload.eventoId() != null ? payload.eventoId() : aprovacao.getEventoId();

        if (eventoId == null) {
            throw new IllegalArgumentException("eventoId deve estar presente no payload de aprovação ou na entidade");
        }

        EventoResponse response = atualizarEventoUseCase.executeApprovedUpdate(
                eventoId,
                atualizarEventoUseCase.restaurarDoPayloadAprovacao(payload));

        aprovacao.setEventoId(response.id());

        return new DecisaoAprovacaoResponse.ActionExecution(
                "EXECUTED",
                response.id(),
                response.status() == null ? null : response.status().name(),
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
