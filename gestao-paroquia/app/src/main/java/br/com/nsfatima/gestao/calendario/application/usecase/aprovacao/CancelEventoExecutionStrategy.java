package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayload;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalExecutionStrategy;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoCanceladoResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.CancelEventoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CancelEventoExecutionStrategy implements ApprovalExecutionStrategy {

    private final CancelEventoUseCase cancelEventoUseCase;
    private final EventoJpaRepository eventoRepository;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;

    public CancelEventoExecutionStrategy(
            CancelEventoUseCase cancelEventoUseCase,
            EventoJpaRepository eventoRepository,
            ApprovalActionPayloadMapper approvalActionPayloadMapper) {
        this.cancelEventoUseCase = cancelEventoUseCase;
        this.eventoRepository = eventoRepository;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
    }

    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.CANCELAMENTO;
    }

    @Override
    public AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        ApprovalActionPayload payload = approvalActionPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
        UUID eventoId = payload.eventoId() != null ? payload.eventoId() : aprovacao.getEventoId();
        
        if (eventoId == null) {
            throw new IllegalArgumentException("eventoId must be present in approval payload or entity for cancellation");
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
            ApprovalActionPayload payload = approvalActionPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
            eventoId = payload.eventoId();
        }
        
        if (eventoId != null) {
            return eventoRepository.findStatusByIdNoLock(eventoId).orElse(null);
        }
        return null;
    }
}
