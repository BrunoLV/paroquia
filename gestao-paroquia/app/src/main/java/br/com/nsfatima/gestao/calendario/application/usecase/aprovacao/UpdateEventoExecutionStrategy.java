package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayload;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalExecutionStrategy;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.UpdateEventoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UpdateEventoExecutionStrategy implements ApprovalExecutionStrategy {

    private final UpdateEventoUseCase updateEventoUseCase;
    private final EventoJpaRepository eventoRepository;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;

    public UpdateEventoExecutionStrategy(
            UpdateEventoUseCase updateEventoUseCase,
            EventoJpaRepository eventoRepository,
            ApprovalActionPayloadMapper approvalActionPayloadMapper) {
        this.updateEventoUseCase = updateEventoUseCase;
        this.eventoRepository = eventoRepository;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
    }

    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.EDICAO_EVENTO;
    }

    @Override
    public AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        ApprovalActionPayload payload = approvalActionPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
        UUID eventoId = payload.eventoId() != null ? payload.eventoId() : aprovacao.getEventoId();

        if (eventoId == null) {
            throw new IllegalArgumentException("eventoId must be present in approval payload or entity");
        }

        EventoResponse response = updateEventoUseCase.executeApprovedUpdate(
                eventoId,
                updateEventoUseCase.restoreFromApprovalPayload(payload));

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
            ApprovalActionPayload payload = approvalActionPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
            eventoId = payload.eventoId();
        }
        
        if (eventoId != null) {
            return eventoRepository.findStatusByIdNoLock(eventoId).orElse(null);
        }
        return null;
    }
}
