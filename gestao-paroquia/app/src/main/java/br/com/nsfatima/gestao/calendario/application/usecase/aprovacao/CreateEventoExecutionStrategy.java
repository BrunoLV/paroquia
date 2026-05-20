package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayload;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalExecutionStrategy;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.CreateEventoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import org.springframework.stereotype.Component;

@Component
public class CreateEventoExecutionStrategy implements ApprovalExecutionStrategy {

    private final CreateEventoUseCase createEventoUseCase;
    private final EventoJpaRepository eventoRepository;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;

    public CreateEventoExecutionStrategy(
            CreateEventoUseCase createEventoUseCase,
            EventoJpaRepository eventoRepository,
            ApprovalActionPayloadMapper approvalActionPayloadMapper) {
        this.createEventoUseCase = createEventoUseCase;
        this.eventoRepository = eventoRepository;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
    }

    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.CRIACAO_EVENTO;
    }

    @Override
    public AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        ApprovalActionPayload payload = approvalActionPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
        String idempotencyKey = payload.idempotencyKey() == null
                ? "approval-exec-" + aprovacao.getId()
                : payload.idempotencyKey();

        EventoResponse response = createEventoUseCase.executeApprovedCreation(
                createEventoUseCase.restoreFromApprovalPayload(payload),
                idempotencyKey);

        aprovacao.setEventoId(response.id());

        return new AprovacaoDecisionResponse.ActionExecution(
                "EXECUTED",
                response.id(),
                response.status() == null ? null : response.status().name(),
                null);
    }

    @Override
    public String fetchResourceStatus(AprovacaoEntity aprovacao) {
        if (aprovacao.getEventoId() != null) {
            return eventoRepository.findStatusByIdNoLock(aprovacao.getEventoId()).orElse(null);
        }
        return null;
    }
}
