package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayload;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CreateEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoApprovalPendingResponse;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateEventoApprovalRequestUseCase {

    private final AprovacaoJpaRepository aprovacaoRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final EventoAuditPublisher auditPublisher;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;

    public CreateEventoApprovalRequestUseCase(
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
     * Records a formal request to create a new event that requires higher-level authorization due to its sensitivity or the proposer's role.
     * 
     * Usage Example:
     * {@code
     * useCase.execute("idempotency-key", createRequest);
     * }
     */
    @Transactional
    public EventoApprovalPendingResponse execute(String idempotencyKey, CreateEventoRequest request) {
        EventoActorContext context = actorContextResolver.resolveRequired();

        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
        entity.setStatus(AprovacaoStatus.PENDENTE.name());
        entity.setSolicitanteId(context.actor());
        entity.setSolicitantePapel(context.role());
        entity.setSolicitanteTipoOrganizacao(context.organizationType());
        entity.setAprovadorPapel(AprovadorPapel.CONSELHO_COORDENADOR.storedValue());
        entity.setCriadoEmUtc(Instant.now());

        ApprovalActionPayload payload = buildPayload(idempotencyKey, request);
        entity.setActionPayloadJson(approvalActionPayloadMapper.toJson(payload));

        aprovacaoRepository.save(entity);

        auditPublisher.publish(context.actor(), "approval-decision-request", entity.getId().toString(), "success");

        return new EventoApprovalPendingResponse(
                entity.getId(),
                entity.getStatus(),
                "APPROVAL_PENDING");
    }

    private ApprovalActionPayload buildPayload(String idempotencyKey, CreateEventoRequest request) {
        return new ApprovalActionPayload(
                idempotencyKey,
                null, // eventoId
                request.titulo(),
                request.descricao(),
                request.categoria(),
                request.organizacaoResponsavelId(),
                request.projetoId(),
                request.inicio(),
                request.fim(),
                request.status(),
                request.adicionadoExtraJustificativa(),
                null, // canceladoMotivo
                null, // motivo
                request.participantes());
    }
}
