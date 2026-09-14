package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.strategy;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.CriarEventoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import org.springframework.stereotype.Component;

/**
 * Estratégia de execução do fluxo de aprovação para solicitações de criação de evento
 * (tipo {@link TipoSolicitacaoInput#CRIACAO_EVENTO}). Quando uma criação é desviada
 * para aprovação (ator sem nível hierárquico suficiente), esta estratégia reconstrói o
 * pedido de criação a partir do payload salvo na solicitação e o executa de forma
 * idempotente, usando uma chave derivada do id da aprovação quando o payload original
 * não trouxer uma {@code Idempotency-Key} explícita — evitando duplicidade caso a
 * execução da aprovação seja repetida.
 */
@Component
public class CreateEventoExecutionStrategy implements EstrategiaExecucaoAprovacao {

    private final CriarEventoUseCase createEventoUseCase;
    private final EventoJpaRepository eventoRepository;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    public CreateEventoExecutionStrategy(
            CriarEventoUseCase createEventoUseCase,
            EventoJpaRepository eventoRepository,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper) {
        this.createEventoUseCase = createEventoUseCase;
        this.eventoRepository = eventoRepository;
        this.aprovacaoAcaoPayloadMapper = aprovacaoAcaoPayloadMapper;
    }

    /**
     * Indica se esta estratégia trata o tipo de solicitação informado.
     *
     * @param tipo tipo da solicitação de aprovação
     * @return {@code true} apenas para {@link TipoSolicitacaoInput#CRIACAO_EVENTO}
     */
    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.CRIACAO_EVENTO;
    }

    /**
     * Cria o evento após a aprovação da solicitação, restaurando o pedido de criação
     * original a partir do payload da ação e executando-o de forma idempotente (usando
     * a chave do payload, ou uma derivada do id da aprovação se ausente), registrando
     * o id do evento criado na entidade de aprovação para rastreabilidade.
     *
     * @param aprovacao entidade de aprovação já decidida, contendo o payload da ação
     * @param actorContext contexto do ator que decidiu a aprovação (não utilizado
     *                       diretamente na criação, mantido pela assinatura comum da
     *                       estratégia de execução)
     * @return resultado da execução com status "EXECUTED", id e status do evento criado
     */
    @Override
    public AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao,
                                                             EventoActorContext actorContext) {

        AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
        String idempotencyKey = payload.idempotencyKey() == null
                ? "aprovacao-exec-" + aprovacao.getId()
                : payload.idempotencyKey();

        EventoResponse response = createEventoUseCase.executeApprovedCreation(
                createEventoUseCase.restaurarDoPayloadAprovacao(payload),
                idempotencyKey);

        aprovacao.setEventoId(response.id());

        return new AprovacaoDecisionResponse.ActionExecution(
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
     * @return status atual do evento, ou {@code null} se ainda não houver evento criado
     */
    @Override
    public String fetchResourceStatus(AprovacaoEntity aprovacao) {
        if (aprovacao.getEventoId() != null) {
            return eventoRepository.findStatusByIdNoLock(aprovacao.getEventoId()).orElse(null);
        }
        return null;
    }
}
