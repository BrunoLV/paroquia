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

    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.CANCELAMENTO;
    }

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
