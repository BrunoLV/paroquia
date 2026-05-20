package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CancelEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoCanceladoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CancelamentoPendenteResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoOperationResult;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayload;
import br.com.nsfatima.gestao.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.gestao.calendario.domain.exception.InvalidStatusTransitionException;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.calendario.application.usecase.observacao.RegisterSystemObservacaoUseCase;
import br.com.nsfatima.gestao.calendario.domain.service.EventoCancelamentoAuthorizationService;
import br.com.nsfatima.gestao.calendario.domain.service.EventoCancelamentoAuthorizationService.CancelamentoRequestMode;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.config.CacheConfig;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for cancelling an event, either immediately or via an approval flow.
 */
@Service
public class CancelEventoUseCase {

    private final EventoJpaRepository eventoJpaRepository;
    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final EventoCancelamentoAuthorizationService authorizationService;
    private final RegisterSystemObservacaoUseCase registerSystemObservacaoUseCase;
    private final EventoAuditPublisher auditPublisher;
    private final CadastroEventoMetricsPublisher metricsPublisher;
    private final ApprovalActionPayloadMapper payloadMapper;
    private final CacheManager cacheManager;

    public CancelEventoUseCase(
            EventoJpaRepository eventoJpaRepository,
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoActorContextResolver actorContextResolver,
            EventoCancelamentoAuthorizationService authorizationService,
            RegisterSystemObservacaoUseCase registerSystemObservacaoUseCase,
            EventoAuditPublisher auditPublisher,
            CadastroEventoMetricsPublisher metricsPublisher,
            ApprovalActionPayloadMapper payloadMapper,
            CacheManager cacheManager) {
        this.eventoJpaRepository = eventoJpaRepository;
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.registerSystemObservacaoUseCase = registerSystemObservacaoUseCase;
        this.auditPublisher = auditPublisher;
        this.metricsPublisher = metricsPublisher;
        this.payloadMapper = payloadMapper;
        this.cacheManager = cacheManager;
    }

    /**
     * Attempts to cancel an event. Returns 200 OK for immediate cancellation or 202 ACCEPTED if approval is needed.
     * 
     * Usage Example:
     * useCase.execute(id, new CancelEventoRequest("Personal reasons"));
     */
    @Transactional
    public EventoOperationResult execute(UUID eventoId, CancelEventoRequest request) {
        EventoEntity evento = findEntity(eventoId);
        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        
        validateCancellableStatus(evento);
        
        CancelamentoRequestMode mode = authorizationService.resolveRequestMode(actorContext, evento.getOrganizacaoResponsavelId());
        if (mode == CancelamentoRequestMode.IMMEDIATE) {
            return processImmediate(evento, request, actorContext);
        }

        return processApproval(evento, request, actorContext);
    }

    /**
     * Finalizes the cancellation process for events that have been vetted and approved, ensuring all side effects like audit logs and system notifications are triggered.
     * 
     * Usage Example:
     * {@code
     * useCase.executeApprovedCancellation(eventoId, "Aprovado pela coordenação", "admin", usuarioId);
     * }
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventoCanceladoResponse executeApprovedCancellation(UUID eventoId, String motivo, String actor, UUID usuarioId) {
        EventoEntity evento = findEntity(eventoId);
        validateCancellableStatus(evento);
        metricsPublisher.publishCancellationFlow("EXECUTED_AFTER_APPROVAL", "SUCCESS");
        return applyCancellation(evento, motivo, actor, usuarioId, "executed-after-approval");
    }

    private EventoEntity findEntity(UUID eventoId) {
        return eventoJpaRepository.findById(Objects.requireNonNull(eventoId))
                .orElseThrow(() -> new EventoNotFoundException(eventoId));
    }

    private void validateCancellableStatus(EventoEntity evento) {
        if (EventoStatusInput.valueOf(evento.getStatus()) != EventoStatusInput.CONFIRMADO) {
            throw new InvalidStatusTransitionException(evento.getStatus(), EventoStatusInput.CANCELADO.name(), "Only CONFIRMADO events can be cancelled");
        }
    }

    private EventoOperationResult processImmediate(EventoEntity evento, CancelEventoRequest request, EventoActorContext actorContext) {
        EventoCanceladoResponse response = applyCancellation(evento, request.motivo(), actorContext.actor(), actorContext.usuarioId(), "success");
        metricsPublisher.publishCancellationFlow("IMMEDIATE", "SUCCESS");
        return new EventoOperationResult.Success(response, HttpStatus.OK);
    }

    private EventoOperationResult processApproval(EventoEntity evento, CancelEventoRequest request, EventoActorContext actorContext) {
        CancelamentoPendenteResponse response = createPendingApproval(evento, request, actorContext);
        metricsPublisher.publishCancellationFlow("PENDING_CREATED", "PENDING");
        return new EventoOperationResult.Pending(response, HttpStatus.ACCEPTED);
    }

    private EventoCanceladoResponse applyCancellation(EventoEntity evento, String motivo, String actor, UUID usuarioId, String auditResult) {
        evento.setStatus(EventoStatusInput.CANCELADO.name());
        evento.setCanceladoMotivo(motivo);
        EventoEntity saved = Objects.requireNonNull(eventoJpaRepository.save(evento));

        registerSystemObservacaoUseCase.execute(saved.getId(), TipoObservacaoInput.CANCELAMENTO, motivo, usuarioId, actor, "cancelamento");

        metricsPublisher.publishAdministrativeRework(false, true, false);
        auditPublisher.publish(actor, "cancel", saved.getId().toString(), auditResult, Map.of(
                        "eventoId", saved.getId(),
                        "organizacaoId", saved.getOrganizacaoResponsavelId(),
                        "status", saved.getStatus(),
                        "motivo", motivo,
                        "tipoObservacao", TipoObservacaoInput.CANCELAMENTO.name()));

        evictProjectCache(saved.getProjetoId());

        return new EventoCanceladoResponse(saved.getId(), saved.getStatus(), saved.getCanceladoMotivo(), saved.getTitulo(),
                saved.getInicioUtc(), saved.getFimUtc(), saved.getOrganizacaoResponsavelId());
    }

    private void evictProjectCache(UUID projetoId) {
        if (projetoId != null) {
            Optional.ofNullable(cacheManager.getCache(CacheConfig.PROJECT_RESUMO_CACHE))
                    .ifPresent(cache -> cache.evict(projetoId));
        }
    }

    private CancelamentoPendenteResponse createPendingApproval(EventoEntity evento, CancelEventoRequest request, EventoActorContext actorContext) {
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(UUID.randomUUID());
        aprovacao.setEventoId(evento.getId());
        aprovacao.setTipoSolicitacao(TipoSolicitacaoInput.CANCELAMENTO.name());
        aprovacao.setAprovadorPapel(AprovadorPapel.CONSELHO_COORDENADOR);
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setSolicitanteId(actorContext.usuarioId().toString());
        aprovacao.setSolicitantePapel(actorContext.role());
        aprovacao.setSolicitanteTipoOrganizacao(actorContext.organizationType());
        aprovacao.setMotivoCancelamentoSnapshot(request.motivo());
        aprovacao.setActionPayloadJson(payloadMapper.toJson(new ApprovalActionPayload(
                null, evento.getId(), null, null, null, null, null, null, null, null, 
                null, null, request.motivo() != null ? request.motivo() : "", null)));
        aprovacao.setCorrelationId(evento.getId().toString());
        aprovacaoJpaRepository.save(aprovacao);

        auditPublisher.publishCancellationPending(actorContext.actor(), evento.getId().toString(), aprovacao.getId().toString(), request.motivo());

        return new CancelamentoPendenteResponse(aprovacao.getId(), aprovacao.getStatusEnum().name(), evento.getId(), "APPROVAL_PENDING");
    }
}
