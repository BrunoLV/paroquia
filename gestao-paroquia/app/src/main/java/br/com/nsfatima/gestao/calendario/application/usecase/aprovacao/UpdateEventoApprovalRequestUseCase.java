package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayload;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoApprovalPendingResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEventoApprovalRequestUseCase {

    private final AprovacaoJpaRepository aprovacaoRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final EventoAuditPublisher auditPublisher;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;

    public UpdateEventoApprovalRequestUseCase(
            AprovacaoJpaRepository aprovacaoRepository,
            EventoActorContextResolver actorContextResolver,
            EventoAuditPublisher auditPublisher,
            ApprovalActionPayloadMapper approvalActionPayloadMapper) {
        this.aprovacaoRepository = aprovacaoRepository;
        this.actorContextResolver = actorContextResolver;
        this.auditPublisher = auditPublisher;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
    }

    /**
     * Submits a request to modify critical fields of an existing event, ensuring that changes to dates or core details are vetted by the Parish Council.
     * 
     * Usage Example:
     * {@code
     * useCase.execute(eventoId, updateRequest);
     * }
     */
    @Transactional
    public EventoApprovalPendingResponse execute(UUID eventoId, UpdateEventoRequest request) {
        EventoActorContext context = actorContextResolver.resolveRequired();

        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setEventoId(eventoId);
        entity.setTipoSolicitacao(TipoSolicitacaoInput.EDICAO_EVENTO.name());
        entity.setStatus(AprovacaoStatus.PENDENTE.name());
        entity.setSolicitanteId(context.actor());
        entity.setSolicitantePapel(context.role());
        entity.setSolicitanteTipoOrganizacao(context.organizationType());
        entity.setAprovadorPapel(AprovadorPapel.CONSELHO_COORDENADOR.storedValue());
        entity.setCriadoEmUtc(Instant.now());
        
        ApprovalActionPayload payload = buildPayload(eventoId, request);
        entity.setActionPayloadJson(approvalActionPayloadMapper.toJson(payload));

        aprovacaoRepository.save(entity);

        auditPublisher.publish(context.actor(), "approval-decision-request", entity.getId().toString(), "success", Map.of(
                "tipoSolicitacao", entity.getTipoSolicitacao(),
                "targetEventId", eventoId
        ));

        return new EventoApprovalPendingResponse(
                entity.getId(),
                entity.getStatus(),
                "APPROVAL_PENDING");
    }

    private ApprovalActionPayload buildPayload(UUID eventoId, UpdateEventoRequest request) {
        return new ApprovalActionPayload(
                null, // idempotencyKey
                eventoId,
                request.titulo(),
                request.descricao(),
                request.categoria(),
                request.organizacaoResponsavelId(),
                request.projetoId(),
                request.inicio(),
                request.fim(),
                null, // status
                request.adicionadoExtraJustificativa(),
                request.canceladoMotivo(),
                null, // motivo
                request.participantes());
    }
}
