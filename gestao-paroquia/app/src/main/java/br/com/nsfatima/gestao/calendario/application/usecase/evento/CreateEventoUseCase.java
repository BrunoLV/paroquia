package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CreateEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoEnvolvidoInput;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoOperationResult;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;
import br.com.nsfatima.gestao.calendario.application.service.EventoIdempotencyService;
import br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.CreateEventoSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.gestao.calendario.domain.policy.BloqueioAnoParoquialPolicy;
import br.com.nsfatima.gestao.calendario.application.service.ValidadorPrioridadeAgenda;
import br.com.nsfatima.gestao.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.gestao.calendario.application.service.EventoPatchAuthorizationService;
import br.com.nsfatima.gestao.calendario.domain.policy.CreateRequestMode;
import br.com.nsfatima.gestao.calendario.application.service.MotorLiturgico;
import br.com.nsfatima.gestao.calendario.application.service.ProvisionamentoLiturgicoService;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AnoLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.PapelEnvolvido;
import br.com.nsfatima.gestao.calendario.infrastructure.config.CacheConfig;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.projeto.application.service.ValidadorVinculoProjeto;
import br.com.nsfatima.gestao.reserva.application.usecase.EventoReservaIntegrationService;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case responsible for creating new events, handling idempotency, authorization, and aprovacao flows.
 */
@Service
public class CreateEventoUseCase {

    private final EventoDomainService domainService;
    private final EventoJpaRepository repository;
    private final EventoEnvolvidoJpaRepository involvementRepository;
    private final EventoMapper mapper;
    private final EventoIdempotencyService idempotencyService;
    private final EventoAuditPublisher auditPublisher;
    private final CadastroEventoMetricsPublisher metricsPublisher;
    private final EventoPatchAuthorizationService authorizationService;
    private final EventoActorContextResolver actorContextResolver;
    private final CreateEventoSolicitacaoAprovacaoUseCase aprovacaoRequestUseCase;
    private final UpdateEventoEnvolvidosUseCase updateEnvolvidosUseCase;
    private final ValidadorVinculoProjeto validadorVinculoProjeto;
    private final BloqueioAnoParoquialPolicy bloqueioAnoParoquialPolicy;
    private final AnoParoquialJpaRepository anoParoquialRepository;
    private final ValidadorPrioridadeAgenda agendaPriorityValidatorService;
    private final CacheManager cacheManager;
    private final MotorLiturgico liturgicalEngine;
    private final CicloLiturgicoRepository cicloLiturgicoRepository;
    private final ProvisionamentoLiturgicoService liturgicalProvisioningService;

    private final EventoReservaIntegrationService eventoReservaIntegrationService;

    public CreateEventoUseCase(
            EventoDomainService domainService,
            EventoJpaRepository repository,
            EventoEnvolvidoJpaRepository involvementRepository,
            EventoMapper mapper,
            EventoIdempotencyService idempotencyService,
            EventoAuditPublisher auditPublisher,
            CadastroEventoMetricsPublisher metricsPublisher,
            EventoPatchAuthorizationService authorizationService,
            EventoActorContextResolver actorContextResolver,
            CreateEventoSolicitacaoAprovacaoUseCase aprovacaoRequestUseCase,
            UpdateEventoEnvolvidosUseCase updateEnvolvidosUseCase,
            ValidadorVinculoProjeto validadorVinculoProjeto,
            BloqueioAnoParoquialPolicy bloqueioAnoParoquialPolicy,
            AnoParoquialJpaRepository anoParoquialRepository,
            ValidadorPrioridadeAgenda agendaPriorityValidatorService,
            CacheManager cacheManager,
            MotorLiturgico liturgicalEngine,
            CicloLiturgicoRepository cicloLiturgicoRepository,
            ProvisionamentoLiturgicoService liturgicalProvisioningService,
            EventoReservaIntegrationService eventoReservaIntegrationService) {
        this.domainService = domainService;
        this.repository = repository;
        this.involvementRepository = involvementRepository;
        this.mapper = mapper;
        this.idempotencyService = idempotencyService;
        this.auditPublisher = auditPublisher;
        this.metricsPublisher = metricsPublisher;
        this.authorizationService = authorizationService;
        this.actorContextResolver = actorContextResolver;
        this.aprovacaoRequestUseCase = aprovacaoRequestUseCase;
        this.updateEnvolvidosUseCase = updateEnvolvidosUseCase;
        this.validadorVinculoProjeto = validadorVinculoProjeto;
        this.bloqueioAnoParoquialPolicy = bloqueioAnoParoquialPolicy;
        this.anoParoquialRepository = anoParoquialRepository;
        this.agendaPriorityValidatorService = agendaPriorityValidatorService;
        this.cacheManager = cacheManager;
        this.liturgicalEngine = liturgicalEngine;
        this.cicloLiturgicoRepository = cicloLiturgicoRepository;
        this.liturgicalProvisioningService = liturgicalProvisioningService;
        this.eventoReservaIntegrationService = eventoReservaIntegrationService;
    }

    /**
     * Executes the creation request. Returns 201 CREATED with the event or 202 ACCEPTED if aprovacao is needed.
     * <p>
     * Usage Example:
     * useCase.execute("idemp-001", new CreateEventoRequest("Title", "Desc", ...));
     */
    @Transactional
    public EventoOperationResult execute(String idempotencyKey, CreateEventoRequest request) {
        validateIdempotencyKey(idempotencyKey);
        validateCreationRequest(request);

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        if (request.isBypassAgendaConflict()) {
            authorizationService.assertCanBypassAgendaConflict(actorContext);
        }
        CreateRequestMode mode = authorizationService.resolveCreateRequestMode(actorContext,
                request.grupoParoquialResponsavelId(), request.igrejaVinculoId());


        if (mode == CreateRequestMode.REQUIRES_APROVACAO) {
            return new EventoOperationResult.Pendente(aprovacaoRequestUseCase.execute(idempotencyKey,
                    request), HttpStatus.ACCEPTED);

        }

        return new EventoOperationResult.Success(createImmediate(idempotencyKey,
                request, actorContext.actor()), HttpStatus.CREATED);

    }

    /**
     * Executes a creation that has already been approved through the aprovacao flow.
     */
    @Transactional
    public EventoResponse executeApprovedCreation(CreateEventoRequest request, String idempotencyKey) {
        return createImmediate(idempotencyKey, request, "fluxo-aprovacao");
    }

    private void validateIdempotencyKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Cabeçalho Idempotency-Key é obrigatório");
        }
    }

    private void validateCreationRequest(CreateEventoRequest request) {
        EventoStatusInput status = request.status() == null ? EventoStatusInput.RASCUNHO : request.status();
        domainService.validateEvento(request.inicio(), request.fim(), status, request.adicionadoExtraJustificativa());
        validarBloqueioAnoParoquial(request.inicio(), status);
        domainService.validateGrupoParoquialParticipantes(
                request.grupoParoquialResponsavelId(), request.participantes());
        validadorVinculoProjeto.validarEvento(request.projetoId(), request.inicio(), request.fim());

        List<EventoEntity> autoCancel = agendaPriorityValidatorService.validarPrioridadeEConflitos(
                request.categoria(),
                request.inicio(),
                request.fim(),
                request.localId(),
                request.igrejaVinculoId(),
                null,
                request.isBypassAgendaConflict());
        agendaPriorityValidatorService.processarCancelamentosAutomaticos(
                autoCancel,
                "Cancelado automaticamente devido a agendamento"
                + " de Solenidade Global / Evento de Maior Prioridade");
    }

    private void validarBloqueioAnoParoquial(Instant inicio, EventoStatusInput statusEvento) {
        AnoParoquialStatus statusAno = inicio == null ? null : anoParoquialRepository
                .findByAno(inicio.atZone(ZoneOffset.UTC).getYear())
                .map(ano -> ano.getStatus())
                .orElse(null);
        bloqueioAnoParoquialPolicy.assertPermitido(inicio, statusAno, statusEvento);
    }

    private EventoResponse createImmediate(String idempotencyKey, CreateEventoRequest request, String actor) {
        validateCreationRequest(request);
        try {
            EventoIdempotencyService.IdempotencyResult result = idempotencyService.execute(idempotencyKey,
                    request, () -> persistNewEvent(request));

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

        if (liturgicalProvisioningService != null) {
            AnoLiturgicoEntity anoLiturgico = liturgicalProvisioningService.resolveAndEnsure(request.inicio());
            entity.setAnoLiturgicoRef(anoLiturgico.getAno());
        } else if (liturgicalEngine != null) {
            int anoLiturgicoRef = liturgicalEngine.resolveAnoLiturgico(request.inicio());
            entity.setAnoLiturgicoRef(anoLiturgicoRef);
        }
        if (cicloLiturgicoRepository != null) {
            cicloLiturgicoRepository.findAll().stream().findFirst()
                    .ifPresent(c -> entity.setCicloLiturgicoId(c.getId()));
        }

        boolean hasOverlap = repository.existsByInicioUtcLessThanAndFimUtcGreaterThan(request.fim(), request.inicio());
        entity.setEstadoConflito(domainService.resolveEstadoConflito(hasOverlap));
        entity.setMotivoConflito(domainService.resolveMotivoConflito(hasOverlap));

        EventoEntity saved = Objects.requireNonNull(repository.save(entity));

        if (eventoReservaIntegrationService != null) {
            UUID solicitanteId = actorContextResolver.resolveRequired().usuarioId();
            eventoReservaIntegrationService.criarReservasEvento(
                    saved.getId(),
                    saved.getTitulo(),
                    saved.getLocalId(),
                    request.ativoIds(),
                    solicitanteId,
                    saved.getGrupoParoquialResponsavelId(),
                    saved.getInicioUtc(),
                    saved.getFimUtc()
            );
        }

        if (request.participantes() != null && !request.participantes().isEmpty()) {
            List<EventoEnvolvidoInput> envolvidos = request.participantes().stream()
                    .map(id -> new EventoEnvolvidoInput(id, PapelEnvolvido.APOIO))
                    .toList();
            updateEnvolvidosUseCase.execute(saved.getId(), envolvidos);
        }

        // Manually fetch involvement to ensure response is complete
        List<EventoEnvolvidoEntity> involvement =
                involvementRepository.findByEventoId(saved.getId());
        saved.definirEnvolvidosCarregados(involvement);

        return mapper.toResponse(saved);
    }

    private void publishCreationOutcomes(EventoIdempotencyService.IdempotencyResult result,
                                         Instant inicio, String actor) {

        EventoResponse response = result.response();
        boolean conflictPendente = EstadoConflitoAgenda.PENDENTE.name().equals(response.estadoConflito());

        metricsPublisher.publishCreateSuccess(conflictPendente, result.replay());
        metricsPublisher.publishEventRegistrationLeadTime(Duration.between(Instant.now(), inicio));

        auditPublisher.publishCreateSuccess(
                actor, response.id().toString(), result.replay(), response.estadoConflito());
    }

    private void handleCreationFailure(String actor, RuntimeException ex) {
        metricsPublisher.publishCreateFailure("NEGOCIO_OU_VALIDACAO");
        auditPublisher.publishCreateFailure(actor, "NEGOCIO_OU_VALIDACAO", ex.getMessage());
    }

    /**
     * Reconstructs the original creation request from stored aprovacao data to allow for final execution after
     * governance vetting.
     * <p>
     * Usage Example:
     * {@code
     * CreateEventoRequest request = useCase.restoreFromAprovacaoPayload(payload);
     * }
     */
    public CreateEventoRequest restoreFromAprovacaoPayload(AprovacaoAcaoPayload payload) {
        Objects.requireNonNull(payload.grupoParoquialResponsavelId(), "grupoParoquialResponsavelId is required");
        Objects.requireNonNull(payload.inicio(), "inicio is required");
        Objects.requireNonNull(payload.fim(), "fim is required");

        return new CreateEventoRequest(payload.titulo(),
                payload.descricao(), payload.categoria(), payload.grupoParoquialResponsavelId(),

                payload.projetoId(),
                payload.localId(), payload.igrejaVinculoId(), payload.inicio(), payload.fim(),
                payload.status(), payload.adicionadoExtraJustificativa(),

                payload.participantes());
    }
}
