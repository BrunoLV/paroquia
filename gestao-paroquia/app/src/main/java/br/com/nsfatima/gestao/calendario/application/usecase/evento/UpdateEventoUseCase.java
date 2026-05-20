package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoEnvolvidoInput;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoOperationResult;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoEditScope;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayload;
import br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.UpdateEventoApprovalRequestUseCase;
import br.com.nsfatima.gestao.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.gestao.projeto.domain.policy.ProjetoVincularPolicy;
import br.com.nsfatima.gestao.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.gestao.calendario.domain.service.EventoPatchAuthorizationService;
import br.com.nsfatima.gestao.calendario.domain.service.EventoPatchAuthorizationService.CreateRequestMode;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.PapelEnvolvido;
import br.com.nsfatima.gestao.calendario.infrastructure.config.CacheConfig;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case responsible for updating event data, handling authorization and approval flows.
 */
@Service
public class UpdateEventoUseCase {

    private final EventoDomainService domainService;
    private final EventoJpaRepository repository;
    private final EventoRecorrenciaJpaRepository recurrenceRepository;
    private final EventoEnvolvidoJpaRepository involvementRepository;
    private final EventoMapper mapper;
    private final EventoPatchAuthorizationService authorizationService;
    private final EventoActorContextResolver actorContextResolver;
    private final UpdateEventoEnvolvidosUseCase updateEnvolvidosUseCase;
    private final ClearEventoEnvolvidosUseCase clearEnvolvidosUseCase;
    private final ProjetoVincularPolicy projetoVincularPolicy;
    private final CadastroEventoMetricsPublisher metricsPublisher;
    private final UpdateEventoApprovalRequestUseCase approvalRequestUseCase;
    private final EventoAuditPublisher auditPublisher;
    private final CacheManager cacheManager;

    public UpdateEventoUseCase(
            EventoDomainService domainService,
            EventoJpaRepository repository,
            EventoRecorrenciaJpaRepository recurrenceRepository,
            EventoEnvolvidoJpaRepository involvementRepository,
            EventoMapper mapper,
            EventoPatchAuthorizationService authorizationService,
            EventoActorContextResolver actorContextResolver,
            UpdateEventoEnvolvidosUseCase updateEnvolvidosUseCase,
            ClearEventoEnvolvidosUseCase clearEnvolvidosUseCase,
            ProjetoVincularPolicy projetoVincularPolicy,
            CadastroEventoMetricsPublisher metricsPublisher,
            UpdateEventoApprovalRequestUseCase approvalRequestUseCase,
            EventoAuditPublisher auditPublisher,
            CacheManager cacheManager) {
        this.domainService = domainService;
        this.repository = repository;
        this.recurrenceRepository = recurrenceRepository;
        this.involvementRepository = involvementRepository;
        this.mapper = mapper;
        this.authorizationService = authorizationService;
        this.actorContextResolver = actorContextResolver;
        this.updateEnvolvidosUseCase = updateEnvolvidosUseCase;
        this.clearEnvolvidosUseCase = clearEnvolvidosUseCase;
        this.projetoVincularPolicy = projetoVincularPolicy;
        this.metricsPublisher = metricsPublisher;
        this.approvalRequestUseCase = approvalRequestUseCase;
        this.auditPublisher = auditPublisher;
        this.cacheManager = cacheManager;
    }

    /**
     * Executes the update request. May return 200 OK with the updated event or 202 ACCEPTED if approval is required.
     * 
     * Usage Example:
     * useCase.execute(id, new UpdateEventoRequest("New Title", null, ...));
     */
    @Transactional
    public EventoOperationResult execute(UUID eventoId, UpdateEventoRequest request) {
        validateRequest(request);
        EventoEntity entity = findEntity(eventoId);
        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        
        validateAuthorization(entity, request, actorContext);

        if (request.changesSensitiveFields() && requiresApproval(entity, actorContext)) {
            return new EventoOperationResult.Pending(approvalRequestUseCase.execute(eventoId, request), HttpStatus.ACCEPTED);
        }

        EventoResponse response = performUpdate(entity, request, actorContext, "success", false);
        return new EventoOperationResult.Success(response, HttpStatus.OK);
    }

    /**
     * Executes an update that has already been approved through the approval flow.
     */
    @Transactional(noRollbackFor = RuntimeException.class)
    public EventoResponse executeApprovedUpdate(UUID eventoId, UpdateEventoRequest request) {
        EventoEntity entity = findEntity(eventoId);
        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        return performUpdate(entity, request, actorContext, "executed", true);
    }

    private void validateRequest(UpdateEventoRequest request) {
        if (request == null || request.isEmptyPayload()) {
            throw new IllegalArgumentException("PATCH payload must not be empty");
        }
    }

    private EventoEntity findEntity(UUID eventoId) {
        return repository.findById(Objects.requireNonNull(eventoId))
                .orElseThrow(() -> new EventoNotFoundException(eventoId));
    }

    private void validateAuthorization(EventoEntity entity, UpdateEventoRequest request, EventoActorContext actorContext) {
        if (request.organizacaoResponsavelId() != null && !request.organizacaoResponsavelId().equals(entity.getOrganizacaoResponsavelId())) {
            authorizationService.assertCanChangeResponsibleOrganization(actorContext);
        } else {
            authorizationService.assertCanEditGeneral(actorContext, entity.getOrganizacaoResponsavelId());
        }

        if (request.participantes() != null) {
            authorizationService.assertCanManageParticipants(actorContext, entity.getOrganizacaoResponsavelId());
        }
    }

    private boolean requiresApproval(EventoEntity entity, EventoActorContext actorContext) {
        return authorizationService.resolveCreateRequestMode(actorContext, entity.getOrganizacaoResponsavelId()) 
                == CreateRequestMode.REQUIRES_APPROVAL;
    }

    private EventoResponse performUpdate(EventoEntity entity, UpdateEventoRequest request, EventoActorContext actorContext, String auditResult, boolean isApprovalFlow) {
        UUID oldProjectId = entity.getProjetoId();
        EventoStatusInput status = request.status() != null ? request.status() : EventoStatusInput.valueOf(entity.getStatus());
        
        validateDomainRules(entity, request, status);
        
        if (request.editScope() == EventoEditScope.ONLY_THIS) {
            entity.setRecorrenciaId(null);
        } else if (request.editScope() == EventoEditScope.THIS_AND_FOLLOWING && entity.getRecorrenciaId() != null) {
            splitRecurrenceSeries(entity);
        }

        mapper.applyPatch(entity, request, status);
        EventoEntity saved = Objects.requireNonNull(repository.save(entity));

        applyParticipantChanges(entity.getId(), request.participantes());
        publishMetrics(entity, request);
        publishAudit(saved, request, actorContext, auditResult, isApprovalFlow);
        
        evictProjectCaches(oldProjectId, saved.getProjetoId());

        return mapper.toResponse(saved);
    }

    private void evictProjectCaches(UUID oldProjectId, UUID newProjectId) {
        Optional.ofNullable(cacheManager.getCache(CacheConfig.PROJECT_RESUMO_CACHE)).ifPresent(cache -> {
            if (oldProjectId != null) {
                cache.evict(oldProjectId);
            }
            if (newProjectId != null && !newProjectId.equals(oldProjectId)) {
                cache.evict(newProjectId);
            }
        });
    }

    private void splitRecurrenceSeries(EventoEntity entity) {
        UUID oldRecorrenciaId = entity.getRecorrenciaId();
        recurrenceRepository.findById(oldRecorrenciaId).ifPresent(oldRule -> {
            // 1. Terminate old rule at previous day
            Instant yesterday = entity.getInicioUtc().minus(Duration.ofDays(1));
            oldRule.setDataFimUtc(yesterday);
            recurrenceRepository.save(oldRule);

            // 2. Create new rule starting from this instance
            EventoRecorrenciaEntity newRule = new EventoRecorrenciaEntity();
            newRule.setId(UUID.randomUUID());
            newRule.setEventoBaseId(entity.getId());
            newRule.setRegra(oldRule.getRegra()); // Same pattern
            recurrenceRepository.save(newRule);

            // 3. Link this and future instances to new rule
            entity.setRecorrenciaId(newRule.getId());
        });
    }

    private void validateDomainRules(EventoEntity entity, UpdateEventoRequest request, EventoStatusInput status) {
        Instant inicio = request.inicio() != null ? request.inicio() : entity.getInicioUtc();
        Instant fim = request.fim() != null ? request.fim() : entity.getFimUtc();
        String justificativa = request.adicionadoExtraJustificativa() != null ? request.adicionadoExtraJustificativa() : entity.getAdicionadoExtraJustificativa();
        
        domainService.validateEvento(inicio, fim, status, justificativa);

        UUID org = request.organizacaoResponsavelId() != null ? request.organizacaoResponsavelId() : entity.getOrganizacaoResponsavelId();
        List<UUID> part = request.participantes() != null ? request.participantes() : 
                involvementRepository.findByEventoId(entity.getId()).stream().map(e -> e.getOrganizacaoId()).toList();
        
        domainService.validateOrganizacaoParticipantes(org, part);

        UUID projetoId = request.projetoId() != null ? request.projetoId() : entity.getProjetoId();
        boolean isRecurring = entity.getRecorrenciaId() != null && request.editScope() != EventoEditScope.ONLY_THIS;
        projetoVincularPolicy.validateLink(projetoId, inicio, fim, isRecurring);
    }

    private void applyParticipantChanges(UUID eventoId, List<UUID> participantes) {
        if (participantes == null) return;
        if (participantes.isEmpty()) {
            clearEnvolvidosUseCase.execute(eventoId);
        } else {
            List<EventoEnvolvidoInput> envolvidos = participantes.stream()
                    .map(id -> new EventoEnvolvidoInput(id, PapelEnvolvido.APOIO))
                    .toList();
            updateEnvolvidosUseCase.execute(eventoId, envolvidos);
        }
    }

    private void publishMetrics(EventoEntity entity, UpdateEventoRequest request) {
        boolean scheduleChanged = request.inicio() != null || request.fim() != null;
        boolean cancellation = request.status() == EventoStatusInput.CANCELADO;
        boolean orgChanged = request.organizacaoResponsavelId() != null && !request.organizacaoResponsavelId().equals(entity.getOrganizacaoResponsavelId());
        
        if (scheduleChanged || cancellation || orgChanged) {
            metricsPublisher.publishAdministrativeRework(scheduleChanged, cancellation, orgChanged);
        }
    }

    private void publishAudit(EventoEntity saved, UpdateEventoRequest request, EventoActorContext actorContext, String result, boolean approvalFlow) {
        boolean scheduleChanged = request.inicio() != null || request.fim() != null;
        boolean cancellation = request.status() == EventoStatusInput.CANCELADO;
        boolean orgChanged = request.organizacaoResponsavelId() != null && !request.organizacaoResponsavelId().equals(saved.getOrganizacaoResponsavelId());

        auditPublisher.publish(actorContext.actor(), "patch", saved.getId().toString(), result, Map.of(
                "organizacaoId", saved.getOrganizacaoResponsavelId(),
                "scheduleChanged", scheduleChanged,
                "cancellation", cancellation,
                "responsibleOrgChanged", orgChanged,
                "sensitiveChange", request.changesSensitiveFields(),
                "approvalFlow", approvalFlow));
    }

    /**
     * Recovers the original modification intent from an approval record, enabling the system to apply the changes once authorization is granted.
     * 
     * Usage Example:
     * {@code
     * UpdateEventoRequest request = useCase.restoreFromApprovalPayload(payload);
     * }
     */
    public UpdateEventoRequest restoreFromApprovalPayload(ApprovalActionPayload payload) {
        return new UpdateEventoRequest(payload.titulo(), payload.descricao(), payload.categoria(), payload.inicio(), payload.fim(),
                payload.status(), payload.adicionadoExtraJustificativa(), payload.canceladoMotivo(),
                payload.organizacaoResponsavelId(), payload.participantes(), null, payload.projetoId());
    }
}
