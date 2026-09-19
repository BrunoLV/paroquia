package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CriarEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoEnvolvidoInput;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoOperationResult;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;
import br.com.nsfatima.gestao.calendario.application.service.EventoIdempotencyService;
import br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.CriarEventoSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.gestao.calendario.domain.policy.BloqueioAnoParoquialPolicy;
import br.com.nsfatima.gestao.calendario.application.service.ValidadorPrioridadeAgenda;
import br.com.nsfatima.gestao.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.gestao.calendario.application.service.EventoPatchAuthorizationService;
import br.com.nsfatima.gestao.calendario.domain.policy.ModoRequisicaoCriacao;
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
 * Caso de uso responsável por criar novos eventos, tratando idempotência, autorização e fluxos de aprovação.
 */
@Service
public class CriarEventoUseCase {

    private final EventoDomainService domainService;
    private final EventoJpaRepository repository;
    private final EventoEnvolvidoJpaRepository envolvimentoRepository;
    private final EventoMapper mapper;
    private final EventoIdempotencyService idempotencyService;
    private final EventoAuditPublisher auditPublisher;
    private final CadastroEventoMetricsPublisher metricsPublisher;
    private final EventoPatchAuthorizationService authorizationService;
    private final EventoActorContextResolver actorContextResolver;
    private final CriarEventoSolicitacaoAprovacaoUseCase aprovacaoRequestUseCase;
    private final AtualizarEventoEnvolvidosUseCase updateEnvolvidosUseCase;
    private final ValidadorVinculoProjeto validadorVinculoProjeto;
    private final BloqueioAnoParoquialPolicy bloqueioAnoParoquialPolicy;
    private final AnoParoquialJpaRepository anoParoquialRepository;
    private final ValidadorPrioridadeAgenda validadorPrioridadeAgendaService;
    private final CacheManager cacheManager;
    private final MotorLiturgico motorLiturgico;
    private final CicloLiturgicoRepository cicloLiturgicoRepository;
    private final ProvisionamentoLiturgicoService provisionamentoLiturgicoService;

    private final EventoReservaIntegrationService eventoReservaIntegrationService;

    public CriarEventoUseCase(
            EventoDomainService domainService,
            EventoJpaRepository repository,
            EventoEnvolvidoJpaRepository envolvimentoRepository,
            EventoMapper mapper,
            EventoIdempotencyService idempotencyService,
            EventoAuditPublisher auditPublisher,
            CadastroEventoMetricsPublisher metricsPublisher,
            EventoPatchAuthorizationService authorizationService,
            EventoActorContextResolver actorContextResolver,
            CriarEventoSolicitacaoAprovacaoUseCase aprovacaoRequestUseCase,
            AtualizarEventoEnvolvidosUseCase updateEnvolvidosUseCase,
            ValidadorVinculoProjeto validadorVinculoProjeto,
            BloqueioAnoParoquialPolicy bloqueioAnoParoquialPolicy,
            AnoParoquialJpaRepository anoParoquialRepository,
            ValidadorPrioridadeAgenda validadorPrioridadeAgendaService,
            CacheManager cacheManager,
            MotorLiturgico motorLiturgico,
            CicloLiturgicoRepository cicloLiturgicoRepository,
            ProvisionamentoLiturgicoService provisionamentoLiturgicoService,
            EventoReservaIntegrationService eventoReservaIntegrationService) {
        this.domainService = domainService;
        this.repository = repository;
        this.envolvimentoRepository = envolvimentoRepository;
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
        this.validadorPrioridadeAgendaService = validadorPrioridadeAgendaService;
        this.cacheManager = cacheManager;
        this.motorLiturgico = motorLiturgico;
        this.cicloLiturgicoRepository = cicloLiturgicoRepository;
        this.provisionamentoLiturgicoService = provisionamentoLiturgicoService;
        this.eventoReservaIntegrationService = eventoReservaIntegrationService;
    }

    /**
     * Executa a requisição de criação. Retorna 201 CREATED com o evento ou 202 ACCEPTED se aprovação for necessária.
     * <p>
     * Exemplo de uso:
     * useCase.execute("idemp-001", new CriarEventoRequest("Titulo", "Descricao", ...));
     */
    @Transactional
    public EventoOperationResult execute(String idempotencyKey, CriarEventoRequest request) {
        validarChaveIdempotencia(idempotencyKey);
        validarRequisicaoCriacao(request);

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        if (request.isBypassAgendaConflict()) {
            authorizationService.assertCanBypassAgendaConflict(actorContext);
        }
        ModoRequisicaoCriacao mode = authorizationService.resolveModoRequisicaoCriacao(actorContext,
                request.grupoParoquialResponsavelId(), request.igrejaVinculoId());


        if (mode == ModoRequisicaoCriacao.REQUER_APROVACAO) {
            return new EventoOperationResult.Pendente(aprovacaoRequestUseCase.execute(idempotencyKey,
                    request), HttpStatus.ACCEPTED);

        }

        return new EventoOperationResult.Success(criarImediato(idempotencyKey,
                request, actorContext.actor()), HttpStatus.CREATED);

    }

    /**
     * Executa uma criação que já foi aprovada através do fluxo de aprovação.
     */
    @Transactional
    public EventoResponse executeApprovedCreation(CriarEventoRequest request, String idempotencyKey) {
        return criarImediato(idempotencyKey, request, "fluxo-aprovacao");
    }

    private void validarChaveIdempotencia(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Cabeçalho Idempotency-Key é obrigatório");
        }
    }

    private void validarRequisicaoCriacao(CriarEventoRequest request) {
        EventoStatusInput status = request.status() == null ? EventoStatusInput.RASCUNHO : request.status();
        domainService.validateEvento(request.inicio(), request.fim(), status, request.adicionadoExtraJustificativa());
        validarBloqueioAnoParoquial(request.inicio(), status);
        domainService.validateGrupoParoquialParticipantes(
                request.grupoParoquialResponsavelId(), request.participantes());
        validadorVinculoProjeto.validarEvento(request.projetoId(), request.inicio(), request.fim());

        List<EventoEntity> autoCancel = validadorPrioridadeAgendaService.validarPrioridadeEConflitos(
                request.categoria(),
                request.inicio(),
                request.fim(),
                request.localId(),
                request.igrejaVinculoId(),
                null,
                request.isBypassAgendaConflict());
        validadorPrioridadeAgendaService.processarCancelamentosAutomaticos(
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

    private EventoResponse criarImediato(String idempotencyKey, CriarEventoRequest request, String actor) {
        validarRequisicaoCriacao(request);
        try {
            EventoIdempotencyService.IdempotencyResult result = idempotencyService.execute(idempotencyKey,
                    request, () -> persistirNovoEvento(request));

            publicarResultadosCriacao(result, request.inicio(), actor);
            evictCacheProjeto(request.projetoId());
            return result.response();
        } catch (RuntimeException ex) {
            tratarFalhaCriacao(actor, ex);
            throw ex;
        }
    }

    private void evictCacheProjeto(UUID projetoId) {
        if (projetoId != null) {
            Optional.ofNullable(cacheManager.getCache(CacheConfig.PROJECT_RESUMO_CACHE))
                    .ifPresent(cache -> cache.evict(projetoId));
        }
    }

    private EventoResponse persistirNovoEvento(CriarEventoRequest request) {
        EventoStatusInput status = request.status() == null ? EventoStatusInput.RASCUNHO : request.status();
        EventoEntity entity = mapper.toNewEntity(request, status);

        if (provisionamentoLiturgicoService != null) {
            AnoLiturgicoEntity anoLiturgico = provisionamentoLiturgicoService.resolveAndEnsure(request.inicio());
            entity.setAnoLiturgicoRef(anoLiturgico.getAno());
        } else if (motorLiturgico != null) {
            int anoLiturgicoRef = motorLiturgico.resolveAnoLiturgico(request.inicio());
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

        // Busca manualmente o envolvimento para garantir que a resposta esteja completa
        List<EventoEnvolvidoEntity> involvement =
                envolvimentoRepository.findByEventoId(saved.getId());
        saved.definirEnvolvidosCarregados(involvement);

        return mapper.toResponse(saved);
    }

    private void publicarResultadosCriacao(EventoIdempotencyService.IdempotencyResult result,
                                         Instant inicio, String actor) {

        EventoResponse response = result.response();
        boolean conflictPendente = EstadoConflitoAgenda.PENDENTE.name().equals(response.estadoConflito());

        metricsPublisher.publishCreateSuccess(conflictPendente, result.replay());
        metricsPublisher.publishEventRegistrationLeadTime(Duration.between(Instant.now(), inicio));

        auditPublisher.publishCreateSuccess(
                actor, response.id().toString(), result.replay(), response.estadoConflito());
    }

    private void tratarFalhaCriacao(String actor, RuntimeException ex) {
        metricsPublisher.publishCreateFailure("NEGOCIO_OU_VALIDACAO");
        auditPublisher.publishCreateFailure(actor, "NEGOCIO_OU_VALIDACAO", ex.getMessage());
    }

    /**
     * Reconstrói a requisição de criação original a partir dos dados de aprovação armazenados, permitindo a
     * execução final após a avaliação de governança.
     * <p>
     * Exemplo de uso:
     * {@code
     * CriarEventoRequest request = useCase.restaurarDoPayloadAprovacao(payload);
     * }
     */
    public CriarEventoRequest restaurarDoPayloadAprovacao(AprovacaoAcaoPayload payload) {
        Objects.requireNonNull(payload.grupoParoquialResponsavelId(), "grupoParoquialResponsavelId é obrigatório");
        Objects.requireNonNull(payload.inicio(), "inicio é obrigatório");
        Objects.requireNonNull(payload.fim(), "fim é obrigatório");

        return new CriarEventoRequest(payload.titulo(),
                payload.descricao(), payload.categoria(), payload.grupoParoquialResponsavelId(),

                payload.projetoId(),
                payload.localId(), payload.igrejaVinculoId(), payload.inicio(), payload.fim(),
                payload.status(), payload.adicionadoExtraJustificativa(),

                payload.participantes());
    }
}
