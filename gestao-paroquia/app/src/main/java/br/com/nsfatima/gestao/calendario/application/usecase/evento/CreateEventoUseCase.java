package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CreateEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoOperationResult;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayload;
import br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.CreateEventoApprovalRequestUseCase;
import br.com.nsfatima.gestao.calendario.domain.policy.CalendarLockPolicy;
import br.com.nsfatima.gestao.projeto.domain.policy.ProjetoVincularPolicy;
import br.com.nsfatima.gestao.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.gestao.calendario.domain.service.EventoPatchAuthorizationService;
import br.com.nsfatima.gestao.calendario.domain.service.EventoPatchAuthorizationService.CreateRequestMode;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.infrastructure.config.CacheConfig;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case responsible for creating new events, handling idempotency, authorization, and approval flows.
 */
@Service
public class CreateEventoUseCase {

    private final EventoDomainService domainService;
    private final EventoJpaRepository repository;
    private final EventoMapper mapper;
    private final EventoIdempotencyService idempotencyService;
    private final EventoAuditPublisher auditPublisher;
    private final CadastroEventoMetricsPublisher metricsPublisher;
    private final EventoPatchAuthorizationService authorizationService;
    private final EventoActorContextResolver actorContextResolver;
    private final CreateEventoApprovalRequestUseCase approvalRequestUseCase;
    private final ProjetoVincularPolicy projetoVincularPolicy;
    private final CalendarLockPolicy calendarLockPolicy;
    private final CacheManager cacheManager;

    public CreateEventoUseCase(
            EventoDomainService domainService,
            EventoJpaRepository repository,
            EventoMapper mapper,
            EventoIdempotencyService idempotencyService,
            EventoAuditPublisher auditPublisher,
            CadastroEventoMetricsPublisher metricsPublisher,
            EventoPatchAuthorizationService authorizationService,
            EventoActorContextResolver actorContextResolver,
            CreateEventoApprovalRequestUseCase approvalRequestUseCase,
            ProjetoVincularPolicy projetoVincularPolicy,
            CalendarLockPolicy calendarLockPolicy,
            CacheManager cacheManager) {
        this.domainService = domainService;
        this.repository = repository;
        this.mapper = mapper;
        this.idempotencyService = idempotencyService;
        this.auditPublisher = auditPublisher;
        this.metricsPublisher = metricsPublisher;
        this.authorizationService = authorizationService;
        this.actorContextResolver = actorContextResolver;
        this.approvalRequestUseCase = approvalRequestUseCase;
        this.projetoVincularPolicy = projetoVincularPolicy;
        this.calendarLockPolicy = calendarLockPolicy;
        this.cacheManager = cacheManager;
    }

    /**
     * Executes the creation request. Returns 201 CREATED with the event or 202 ACCEPTED if approval is needed.
     * 
     * Usage Example:
     * useCase.execute("idemp-001", new CreateEventoRequest("Title", "Desc", ...));
     */
    @Transactional
    public EventoOperationResult execute(String idempotencyKey, CreateEventoRequest request) {
        validateIdempotencyKey(idempotencyKey);
        validateCreationRequest(request);

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        CreateRequestMode mode = authorizationService.resolveCreateRequestMode(actorContext, request.organizacaoResponsavelId());

        if (mode == CreateRequestMode.REQUIRES_APPROVAL) {
            return new EventoOperationResult.Pending(approvalRequestUseCase.execute(idempotencyKey, request), HttpStatus.ACCEPTED);
        }

        return new EventoOperationResult.Success(createImmediate(idempotencyKey, request, actorContext.actor()), HttpStatus.CREATED);
    }

    /**
     * Executes a creation that has already been approved through the approval flow.
     */
    @Transactional
    public EventoResponse executeApprovedCreation(CreateEventoRequest request, String idempotencyKey) {
        return createImmediate(idempotencyKey, request, "approval-flow");
    }

    private void validateIdempotencyKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Idempotency-Key header is required");
        }
    }

    private void validateCreationRequest(CreateEventoRequest request) {
        EventoStatusInput status = request.status() == null ? EventoStatusInput.RASCUNHO : request.status();
        domainService.validateEvento(request.inicio(), request.fim(), status, request.adicionadoExtraJustificativa());
        calendarLockPolicy.checkLock(request.inicio(), status);
        domainService.validateOrganizacaoParticipantes(request.organizacaoResponsavelId(), request.participantes());
        projetoVincularPolicy.validateLink(request.projetoId(), request.inicio(), request.fim(), false);
    }

    private EventoResponse createImmediate(String idempotencyKey, CreateEventoRequest request, String actor) {
        validateCreationRequest(request);
        try {
            EventoIdempotencyService.IdempotencyResult result = idempotencyService.execute(idempotencyKey, request, () -> persistNewEvent(request));
            publishCreationOutcomes(result, request.inicio(), actor);
            evictProjectCache(request.projetoId());
            return result.response();
        } catch (RuntimeException ex) {
            handleCreationFailure(actor, ex);
            throw ex;
        }
    }

    private void evictProjectCache(UUID projetoId) {
        if (projetoId != null) {
            Optional.ofNullable(cacheManager.getCache(CacheConfig.PROJECT_RESUMO_CACHE))
                    .ifPresent(cache -> cache.evict(projetoId));
        }
    }

    private EventoResponse persistNewEvent(CreateEventoRequest request) {
        EventoStatusInput status = request.status() == null ? EventoStatusInput.RASCUNHO : request.status();
        EventoEntity entity = mapper.toNewEntity(request, status);
        
        boolean hasOverlap = repository.existsByInicioUtcLessThanAndFimUtcGreaterThan(request.fim(), request.inicio());
        entity.setConflictState(domainService.resolveConflictState(hasOverlap));
        entity.setConflictReason(domainService.resolveConflictReason(hasOverlap));

        EventoEntity saved = Objects.requireNonNull(repository.save(entity));
        return mapper.toResponse(saved);
    }

    private void publishCreationOutcomes(EventoIdempotencyService.IdempotencyResult result, Instant inicio, String actor) {
        EventoResponse response = result.response();
        boolean conflictPending = "CONFLICT_PENDING".equals(response.conflictState());
        
        metricsPublisher.publishCreateSuccess(conflictPending, result.replay());
        metricsPublisher.publishEventRegistrationLeadTime(Duration.between(Instant.now(), inicio));
        
        auditPublisher.publishCreateSuccess(actor, response.id().toString(), result.replay(), response.conflictState());
    }

    private void handleCreationFailure(String actor, RuntimeException ex) {
        metricsPublisher.publishCreateFailure("BUSINESS_OR_VALIDATION");
        auditPublisher.publishCreateFailure(actor, "BUSINESS_OR_VALIDATION", ex.getMessage());
    }

    /**
     * Reconstructs the original creation request from stored approval data to allow for final execution after governance vetting.
     * 
     * Usage Example:
     * {@code
     * CreateEventoRequest request = useCase.restoreFromApprovalPayload(payload);
     * }
     */
    public CreateEventoRequest restoreFromApprovalPayload(ApprovalActionPayload payload) {
        Objects.requireNonNull(payload.organizacaoResponsavelId(), "organizacaoResponsavelId is required");
        Objects.requireNonNull(payload.inicio(), "inicio is required");
        Objects.requireNonNull(payload.fim(), "fim is required");
        
        return new CreateEventoRequest(payload.titulo(), payload.descricao(), payload.categoria(), payload.organizacaoResponsavelId(),
                payload.projetoId(), payload.inicio(), payload.fim(), payload.status(), payload.adicionadoExtraJustificativa(),
                payload.participantes());
    }
}
