package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.strategy;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.UpdateEventoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UpdateEventoExecutionStrategy implements EstrategiaExecucaoAprovacao {

    private final UpdateEventoUseCase updateEventoUseCase;
    private final EventoJpaRepository eventoRepository;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    public UpdateEventoExecutionStrategy(
            UpdateEventoUseCase updateEventoUseCase,
            EventoJpaRepository eventoRepository,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper) {
        this.updateEventoUseCase = updateEventoUseCase;
        this.eventoRepository = eventoRepository;
        this.aprovacaoAcaoPayloadMapper = aprovacaoAcaoPayloadMapper;
    }

    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.EDICAO_EVENTO;
    }

    @Override
    public AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao,
                                                             EventoActorContext actorContext) {

        AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
        UUID eventoId = payload.eventoId() != null ? payload.eventoId() : aprovacao.getEventoId();

        if (eventoId == null) {
            throw new IllegalArgumentException("eventoId deve estar presente no payload de aprovação ou na entidade");
        }

        EventoResponse response = updateEventoUseCase.executeApprovedUpdate(
                eventoId,
                updateEventoUseCase.restoreFromAprovacaoPayload(payload));

        aprovacao.setEventoId(response.id());

        return new AprovacaoDecisionResponse.ActionExecution(
                "EXECUTED",
                response.id(),
                response.status() == null ? null : response.status().name(),
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
