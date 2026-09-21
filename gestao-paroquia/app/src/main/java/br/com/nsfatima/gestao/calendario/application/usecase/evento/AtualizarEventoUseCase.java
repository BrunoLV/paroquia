package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EscopoEdicaoEvento;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoEnvolvidoInput;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.ResultadoOperacaoEvento;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.AtualizarEventoRequest;
import br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.AtualizarEventoSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.gestao.calendario.domain.exception.EventoNaoEncontradoException;
import br.com.nsfatima.gestao.calendario.domain.exception.ViolacaoRegraNegocioException;
import br.com.nsfatima.gestao.calendario.application.service.ValidadorPrioridadeAgenda;
import br.com.nsfatima.gestao.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.gestao.calendario.application.service.EventoPatchAuthorizationService;
import br.com.nsfatima.gestao.calendario.domain.policy.ModoRequisicaoCriacao;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.FormatoEvento;
import br.com.nsfatima.gestao.calendario.domain.type.PapelEnvolvido;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.calendario.infrastructure.config.CacheConfig;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.projeto.application.service.ValidadorVinculoProjeto;
import br.com.nsfatima.gestao.reserva.application.usecase.EventoReservaIntegrationService;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoLocalEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoLocalJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Caso de uso responsável por atualizar dados de evento, tratando autorização e fluxos de aprovação.
 */
@Service
public class AtualizarEventoUseCase {

    private final EventoDomainService domainService;
    private final EventoJpaRepository repository;
    private final EventoRecorrenciaJpaRepository recurrenceRepository;
    private final EventoEnvolvidoJpaRepository envolvimentoRepository;
    private final EventoMapper mapper;
    private final EventoPatchAuthorizationService authorizationService;
    private final EventoActorContextResolver actorContextResolver;
    private final AtualizarEventoEnvolvidosUseCase updateEnvolvidosUseCase;
    private final LimparEventoEnvolvidosUseCase clearEnvolvidosUseCase;
    private final ValidadorVinculoProjeto validadorVinculoProjeto;
    private final CadastroEventoMetricsPublisher metricsPublisher;
    private final AtualizarEventoSolicitacaoAprovacaoUseCase aprovacaoRequestUseCase;
    private final EventoAuditPublisher auditPublisher;
    private final ValidadorPrioridadeAgenda validadorPrioridadeAgendaService;
    private final CacheManager cacheManager;
    private final EventoReservaIntegrationService eventoReservaIntegrationService;
    private final EventoLocalJpaRepository eventoLocalJpaRepository;

    public AtualizarEventoUseCase(
            EventoDomainService domainService,
            EventoJpaRepository repository,
            EventoRecorrenciaJpaRepository recurrenceRepository,
            EventoEnvolvidoJpaRepository envolvimentoRepository,
            EventoMapper mapper,
            EventoPatchAuthorizationService authorizationService,
            EventoActorContextResolver actorContextResolver,
            AtualizarEventoEnvolvidosUseCase updateEnvolvidosUseCase,
            LimparEventoEnvolvidosUseCase clearEnvolvidosUseCase,
            ValidadorVinculoProjeto validadorVinculoProjeto,
            CadastroEventoMetricsPublisher metricsPublisher,
            AtualizarEventoSolicitacaoAprovacaoUseCase aprovacaoRequestUseCase,
            EventoAuditPublisher auditPublisher,
            ValidadorPrioridadeAgenda validadorPrioridadeAgendaService,
            CacheManager cacheManager,
            EventoReservaIntegrationService eventoReservaIntegrationService) {
        this(domainService, repository, recurrenceRepository, envolvimentoRepository, mapper,
                authorizationService, actorContextResolver, updateEnvolvidosUseCase, clearEnvolvidosUseCase,
                validadorVinculoProjeto, metricsPublisher, aprovacaoRequestUseCase, auditPublisher,
                validadorPrioridadeAgendaService, cacheManager, eventoReservaIntegrationService, null);
    }

    @Autowired
    public AtualizarEventoUseCase(
            EventoDomainService domainService,
            EventoJpaRepository repository,
            EventoRecorrenciaJpaRepository recurrenceRepository,
            EventoEnvolvidoJpaRepository envolvimentoRepository,
            EventoMapper mapper,
            EventoPatchAuthorizationService authorizationService,
            EventoActorContextResolver actorContextResolver,
            AtualizarEventoEnvolvidosUseCase updateEnvolvidosUseCase,
            LimparEventoEnvolvidosUseCase clearEnvolvidosUseCase,
            ValidadorVinculoProjeto validadorVinculoProjeto,
            CadastroEventoMetricsPublisher metricsPublisher,
            AtualizarEventoSolicitacaoAprovacaoUseCase aprovacaoRequestUseCase,
            EventoAuditPublisher auditPublisher,
            ValidadorPrioridadeAgenda validadorPrioridadeAgendaService,
            CacheManager cacheManager,
            EventoReservaIntegrationService eventoReservaIntegrationService,
            @Autowired(required = false) EventoLocalJpaRepository eventoLocalJpaRepository) {
        this.domainService = domainService;
        this.repository = repository;
        this.recurrenceRepository = recurrenceRepository;
        this.envolvimentoRepository = envolvimentoRepository;
        this.mapper = mapper;
        this.authorizationService = authorizationService;
        this.actorContextResolver = actorContextResolver;
        this.updateEnvolvidosUseCase = updateEnvolvidosUseCase;
        this.clearEnvolvidosUseCase = clearEnvolvidosUseCase;
        this.validadorVinculoProjeto = validadorVinculoProjeto;
        this.metricsPublisher = metricsPublisher;
        this.aprovacaoRequestUseCase = aprovacaoRequestUseCase;
        this.auditPublisher = auditPublisher;
        this.validadorPrioridadeAgendaService = validadorPrioridadeAgendaService;
        this.cacheManager = cacheManager;
        this.eventoReservaIntegrationService = eventoReservaIntegrationService;
        this.eventoLocalJpaRepository = eventoLocalJpaRepository;
    }

    /**
     * Executa a requisição de atualização. Pode retornar 200 OK com o evento atualizado ou 202 ACCEPTED
     * caso aprovação seja necessária.
     * <p>
     * Exemplo de uso:
     * useCase.execute(id, new AtualizarEventoRequest("Novo Título", null, ...));
     */
    @Transactional
    public ResultadoOperacaoEvento execute(UUID eventoId, AtualizarEventoRequest request) {
        validateRequest(request);
        EventoEntity entity = findEntity(eventoId);
        EventoActorContext actorContext = actorContextResolver.resolveRequired();

        PapelOrganizacional atorPapel = PapelOrganizacional.fromStoredValue(actorContext.role());
        if (atorPapel == PapelOrganizacional.SECRETARIO) {
            boolean hasJustificativa = (request.adicionadoExtraJustificativa() != null && !request.adicionadoExtraJustificativa().isBlank())
                    || (request.canceladoMotivo() != null && !request.canceladoMotivo().isBlank());
            if (!hasJustificativa) {
                throw new ViolacaoRegraNegocioException("Justificativa é obrigatória para operações submetidas pelo Secretário");
            }
        }

        validateAuthorization(entity, request, actorContext);

        if (request.alteraCamposSensiveis() && requiresAprovacao(entity, actorContext)) {
            return new ResultadoOperacaoEvento.Pendente(aprovacaoRequestUseCase.execute(eventoId,
                    request), HttpStatus.ACCEPTED);

        }

        EventoResponse response = performUpdate(entity, request, actorContext, "sucesso", false);
        return new ResultadoOperacaoEvento.Sucesso(response, HttpStatus.OK);
    }

    /**
     * Executa uma atualização que já foi aprovada através do fluxo de aprovação.
     */
    @Transactional(noRollbackFor = RuntimeException.class)
    public EventoResponse executeApprovedUpdate(UUID eventoId, AtualizarEventoRequest request) {
        EventoEntity entity = findEntity(eventoId);
        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        return performUpdate(entity, request, actorContext, "executado", true);
    }

    private void validateRequest(AtualizarEventoRequest request) {
        if (request == null || request.isPayloadVazio()) {
            throw new IllegalArgumentException("Corpo do PATCH não pode ser vazio");
        }
    }

    private EventoEntity findEntity(UUID eventoId) {
        return repository.findById(Objects.requireNonNull(eventoId))
                .orElseThrow(() -> new EventoNaoEncontradoException(eventoId));
    }

    private void validateAuthorization(EventoEntity entity,
                                       AtualizarEventoRequest request, EventoActorContext actorContext) {

        if (request.grupoParoquialResponsavelId() != null &&
                !request.grupoParoquialResponsavelId().equals(entity.getGrupoParoquialResponsavelId())) {
            authorizationService.assertCanChangeResponsibleOrganization(actorContext);
        } else {
            authorizationService.assertCanEditGeneral(actorContext,
                    entity.getGrupoParoquialResponsavelId(), entity.getIgrejaVinculoId());

        }

        if (request.participantes() != null) {
            authorizationService.assertCanManageParticipants(actorContext,
                    entity.getGrupoParoquialResponsavelId(), entity.getIgrejaVinculoId());

        }

        if (request.isLiberarConflitoAgenda()) {
            authorizationService.assertCanLiberarConflitoAgenda(actorContext);
        }
    }

    private boolean requiresAprovacao(EventoEntity entity, EventoActorContext actorContext) {
        return authorizationService.resolveModoRequisicaoCriacao(actorContext,
                entity.getGrupoParoquialResponsavelId(), entity.getIgrejaVinculoId())

                == ModoRequisicaoCriacao.REQUER_APROVACAO;
    }

    private EventoResponse performUpdate(EventoEntity entity,
                                         AtualizarEventoRequest request, EventoActorContext actorContext,
                                         String auditResult,
                                         boolean isAprovacaoFlow) {

        UUID oldProjectId = entity.getProjetoId();
        EventoStatusInput status =
                request.status() != null ? request.status() : EventoStatusInput.valueOf(entity.getStatus());

        validateDomainRules(entity, request, status);

        if (request.escopoEdicao() == EscopoEdicaoEvento.SOMENTE_ESTE) {
            entity.setRecorrenciaId(null);
        } else if (request.escopoEdicao() == EscopoEdicaoEvento.ESTE_E_PROXIMOS && entity.getRecorrenciaId() != null) {
            splitRecurrenceSeries(entity);
        } else if (request.escopoEdicao() == EscopoEdicaoEvento.TODOS && entity.getRecorrenciaId() != null) {
            updateAllInstances(entity.getRecorrenciaId(), request, status);
        }

        mapper.applyPatch(entity, request, status);
        if (request.participantes() != null) {
            entity.definirEnvolvidosCarregados(List.of());
        }
        EventoEntity saved = Objects.requireNonNull(repository.save(entity));

        if (eventoLocalJpaRepository != null && request.localIds() != null) {
            eventoLocalJpaRepository.deleteByEventoId(saved.getId());
            if (!request.localIds().isEmpty()) {
                List<EventoLocalEntity> novosLocais = request.localIds().stream()
                        .filter(Objects::nonNull)
                        .distinct()
                        .map(locId -> new EventoLocalEntity(saved.getId(), locId))
                        .toList();
                eventoLocalJpaRepository.saveAll(novosLocais);
            }
            saved.definirLocaisCarregados(eventoLocalJpaRepository.findByEventoId(saved.getId()));
        } else if (saved.getLocalId() != null && eventoLocalJpaRepository != null) {
            List<EventoLocalEntity> existentes = eventoLocalJpaRepository.findByEventoId(saved.getId());
            if (existentes.isEmpty()) {
                eventoLocalJpaRepository.save(new EventoLocalEntity(saved.getId(), saved.getLocalId()));
                existentes = eventoLocalJpaRepository.findByEventoId(saved.getId());
            }
            saved.definirLocaisCarregados(existentes);
        }

        if (eventoReservaIntegrationService != null) {
            eventoReservaIntegrationService.atualizarReservasEvento(
                    saved.getId(),
                    saved.getTitulo(),
                    saved.getLocalId(),
                    request.localIds(),
                    request.ativoIds(),
                    actorContext.usuarioId(),
                    saved.getGrupoParoquialResponsavelId(),
                    saved.getInicioUtc(),
                    saved.getFimUtc(),
                    saved.getPeriodosList()
            );
        }

        applyParticipantChanges(entity.getId(), request.participantes());

        // Busca manualmente o envolvimento para garantir resposta completa
        // (especialmente para consistência de carregamento EAGER)
        List<EventoEnvolvidoEntity> involvement = envolvimentoRepository.findByEventoId(saved.getId());
        saved.definirEnvolvidosCarregados(involvement);

        publishMetrics(entity, request);
        publishAudit(saved, request, actorContext, auditResult, isAprovacaoFlow);

        evictProjectCaches(oldProjectId, saved.getProjetoId());

        return mapper.toResponse(saved);
    }

    private void updateAllInstances(UUID recorrenciaId, AtualizarEventoRequest request, EventoStatusInput status) {
        // 1. Atualizar o evento modelo (evento base da regra de recorrência)
        recurrenceRepository.findById(recorrenciaId).flatMap(regra -> repository.findById(regra.getEventoBaseId()))
                .ifPresent(eventoBase -> {
                    AtualizarEventoRequest templateRequest = new AtualizarEventoRequest(
                            request.titulo(), request.descricao(), request.categoria(),
                            null, null, // Don't update template start/end times via instancia update
                            request.status(), request.adicionadoExtraJustificativa(), request.canceladoMotivo(),
                            request.grupoParoquialResponsavelId(), request.participantes(),
                            null, request.projetoId(), request.localId(), request.igrejaVinculoId());
                    mapper.applyPatch(eventoBase, templateRequest, status);
                    repository.save(eventoBase);
                    applyParticipantChanges(eventoBase.getId(), request.participantes());
                    // applyParticipantChanges apaga/recria as linhas de eventos_envolvidos por fora
                    // deste agregado (via EventoEnvolvidoJpaRepository); o campo envolvidos eager
                    // desta instância em memória fica com referências obsoletas para o Hibernate,
                    // que o valida (e rejeita como transiente) em qualquer flush posterior na mesma
                    // transação — mesma causa já tratada no fluxo principal, ver linha ~199.
                    eventoBase.definirEnvolvidosCarregados(List.of());
                });

        // 2. Atualizar todas as instâncias existentes
        List<EventoEntity> allInstances = repository.findByRecorrenciaId(recorrenciaId);
        for (EventoEntity instancia : allInstances) {
            AtualizarEventoRequest siblingRequest = new AtualizarEventoRequest(
                    request.titulo(), request.descricao(), request.categoria(),
                    null, null, // Don't change time for siblings
                    request.status(), request.adicionadoExtraJustificativa(), request.canceladoMotivo(),
                    request.grupoParoquialResponsavelId(), request.participantes(),
                    null, request.projetoId(), request.localId(), request.igrejaVinculoId());

            mapper.applyPatch(instancia, siblingRequest, status);
            repository.save(instancia);
            applyParticipantChanges(instancia.getId(), request.participantes());
            instancia.definirEnvolvidosCarregados(List.of());
        }
    }

    private void evictProjectCaches(UUID oldProjectId, UUID newProjectId) {
        Optional.ofNullable(cacheManager.getCache(CacheConfig.PROJETO_RESUMO_CACHE)).ifPresent(cache -> {
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
        recurrenceRepository.findById(oldRecorrenciaId).ifPresent(regraAnterior -> {
            // 1. Encerrar regra anterior no dia anterior
            Instant yesterday = entity.getInicioUtc().minus(Duration.ofDays(1));
            regraAnterior.setDataFimUtc(yesterday);
            recurrenceRepository.save(regraAnterior);

            // 2. Criar nova regra a partir desta instância
            EventoRecorrenciaEntity newRule = new EventoRecorrenciaEntity();
            newRule.setId(UUID.randomUUID());
            newRule.setEventoBaseId(entity.getId());
            newRule.setRegra(regraAnterior.getRegra()); // Same pattern
            recurrenceRepository.save(newRule);

            // 3. Vincular esta e as futuras instâncias à nova regra
            entity.setRecorrenciaId(newRule.getId());
        });
    }

    private void validateDomainRules(EventoEntity entity, AtualizarEventoRequest request, EventoStatusInput status) {
        FormatoEvento formato = request.formato() != null ? request.formato() : entity.getFormato();
        if (formato == FormatoEvento.PRESENCIAL || formato == FormatoEvento.HIBRIDO) {
            boolean hasLocais;
            if (request.localIds() != null) {
                hasLocais = !request.localIds().isEmpty();
            } else if (request.localId() != null) {
                hasLocais = true;
            } else {
                hasLocais = entity.getLocalId() != null
                        || (entity.getLocais() != null && !entity.getLocais().isEmpty());
            }
            if (!hasLocais) {
                throw new ViolacaoRegraNegocioException("Ao menos um local deve ser informado para eventos presenciais ou híbridos.");
            }
        }

        Instant inicio = request.inicio() != null ? request.inicio() : entity.getInicioUtc();
        Instant fim = request.fim() != null ? request.fim() : entity.getFimUtc();
        String justificativa = request.adicionadoExtraJustificativa() != null ? request.adicionadoExtraJustificativa() :
                entity.getAdicionadoExtraJustificativa();

        domainService.validateEvento(inicio, fim, status, justificativa);

        UUID org = request.grupoParoquialResponsavelId() != null ? request.grupoParoquialResponsavelId() :
                entity.getGrupoParoquialResponsavelId();
        List<UUID> part = request.participantes() != null ? request.participantes() :
                envolvimentoRepository.findByEventoId(entity.getId()).stream().map(
                        EventoEnvolvidoEntity::getGrupoParoquialId).toList();

        domainService.validateGrupoParoquialParticipantes(org, part);

        UUID projetoId = request.projetoId() != null ? request.projetoId() : entity.getProjetoId();
        boolean isRecurring = entity.getRecorrenciaId() != null && request.escopoEdicao() != EscopoEdicaoEvento.SOMENTE_ESTE;
        if (isRecurring && entity.getRecorrenciaId() != null) {
            EventoRecorrenciaEntity recurrence = recurrenceRepository.findById(entity.getRecorrenciaId())
                    .orElseThrow(() -> new IllegalStateException("Recurrence not found: " + entity.getRecorrenciaId()));
            validadorVinculoProjeto.validarRecorrencia(projetoId, inicio, fim, recurrence.getRegra());
        } else {
            validadorVinculoProjeto.validarEvento(projetoId, inicio, fim);
        }

        CategoriaEvento cat = request.categoria() != null
                ? request.categoria()
                : entity.getCategoria();
        UUID local = request.localId() != null ? request.localId() : entity.getLocalId();
        UUID igreja = request.igrejaVinculoId() != null ? request.igrejaVinculoId() : entity.getIgrejaVinculoId();

        List<EventoEntity> autoCancel = validadorPrioridadeAgendaService.validarPrioridadeEConflitos(
                cat,
                inicio,
                fim,
                local,
                igreja,
                entity.getId(),
                request.isLiberarConflitoAgenda());
        validadorPrioridadeAgendaService.processarCancelamentosAutomaticos(
                autoCancel,
                "Cancelado automaticamente devido a agendamento"
                + " de Solenidade Global / Evento de Maior Prioridade");
    }

    private void applyParticipantChanges(UUID eventoId, List<UUID> participantes) {
        if (participantes == null) {
            return;
        }
        if (participantes.isEmpty()) {
            clearEnvolvidosUseCase.execute(eventoId);
        } else {
            List<EventoEnvolvidoInput> envolvidos = participantes.stream()
                    .map(id -> new EventoEnvolvidoInput(id, PapelEnvolvido.APOIO))
                    .toList();
            updateEnvolvidosUseCase.execute(eventoId, envolvidos);
        }
    }

    private void publishMetrics(EventoEntity entity, AtualizarEventoRequest request) {
        boolean scheduleChanged = request.inicio() != null || request.fim() != null;
        boolean cancellation = request.status() == EventoStatusInput.CANCELADO;
        boolean orgChanged = request.grupoParoquialResponsavelId() != null &&
                !request.grupoParoquialResponsavelId().equals(entity.getGrupoParoquialResponsavelId());

        if (scheduleChanged || cancellation || orgChanged) {
            metricsPublisher.publishAdministrativeRework(scheduleChanged, cancellation, orgChanged);
        }

        if (cancellation) {
            metricsPublisher.recordDelecao("EVENTO", "DIRETA");
        } else {
            metricsPublisher.recordEdicao("EVENTO", "DIRETA");
        }
    }

    private void publishAudit(EventoEntity saved,
                              AtualizarEventoRequest request, EventoActorContext actorContext, String result,
                              boolean fluxoAprovacao) {

        boolean scheduleChanged = request.inicio() != null || request.fim() != null;
        boolean cancellation = request.status() == EventoStatusInput.CANCELADO;
        boolean orgChanged = request.grupoParoquialResponsavelId() != null &&
                !request.grupoParoquialResponsavelId().equals(saved.getGrupoParoquialResponsavelId());

        auditPublisher.publish(actorContext.actor(), "atualizar", saved.getId().toString(), result, Map.of(
                "grupoParoquialId", saved.getGrupoParoquialResponsavelId(),
                "agendaAlterada", scheduleChanged,
                "cancelamento", cancellation,
                "grupoParoquialResponsavelAlterada", orgChanged,
                "alteracaoSensivel", request.alteraCamposSensiveis(),
                "fluxoAprovacao", fluxoAprovacao));
    }

    /**
     * Recupera a intenção original de modificação a partir de um registro de aprovação, permitindo que o
     * sistema aplique as mudanças assim que a autorização for concedida.
     * <p>
     * Exemplo de uso:
     * {@code
     * AtualizarEventoRequest request = useCase.restaurarDoPayloadAprovacao(payload);
     * }
     */
    public AtualizarEventoRequest restaurarDoPayloadAprovacao(AprovacaoAcaoPayload payload) {
        return new AtualizarEventoRequest(
                payload.titulo(),
                payload.descricao(),
                payload.categoria(),
                payload.inicio(),
                payload.fim(),
                payload.status(),
                payload.adicionadoExtraJustificativa(),
                payload.canceladoMotivo(),
                payload.grupoParoquialResponsavelId(),
                payload.participantes(),
                null,
                payload.projetoId(),
                payload.localId(),
                payload.localIds(),
                payload.igrejaVinculoId(),
                false,
                payload.ativoIds(),
                payload.tipoHorario(),
                payload.formato(),
                payload.periodos());
    }
}
