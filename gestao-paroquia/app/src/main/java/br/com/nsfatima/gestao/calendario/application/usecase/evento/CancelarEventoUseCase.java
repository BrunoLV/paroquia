package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CancelarEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CancelamentoPendenteResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoCanceladoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.ResultadoOperacaoEvento;
import br.com.nsfatima.gestao.calendario.application.usecase.observacao.RegistrarObservacaoSistemaUseCase;
import br.com.nsfatima.gestao.calendario.domain.exception.EventoNaoEncontradoException;
import br.com.nsfatima.gestao.calendario.domain.exception.TransicaoStatusInvalidaException;
import br.com.nsfatima.gestao.calendario.domain.exception.ViolacaoRegraNegocioException;
import br.com.nsfatima.gestao.calendario.application.service.EventoCancelamentoAuthorizationService;
import br.com.nsfatima.gestao.calendario.domain.policy.ModoRequisicaoCancelamento;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.config.CacheConfig;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.reserva.application.usecase.EventoReservaIntegrationService;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case for cancelling an event, either immediately or via an aprovacao flow.
 */
@Service
public class CancelarEventoUseCase {

    private final EventoJpaRepository eventoJpaRepository;
    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final EventoCancelamentoAuthorizationService authorizationService;
    private final RegistrarObservacaoSistemaUseCase registerSystemObservacaoUseCase;
    private final EventoAuditPublisher auditPublisher;
    private final CadastroEventoMetricsPublisher metricsPublisher;
    private final AprovacaoAcaoPayloadMapper payloadMapper;
    private final CacheManager cacheManager;
    private final EventoReservaIntegrationService eventoReservaIntegrationService;

    public CancelarEventoUseCase(
            EventoJpaRepository eventoJpaRepository,
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoActorContextResolver actorContextResolver,
            EventoCancelamentoAuthorizationService authorizationService,
            RegistrarObservacaoSistemaUseCase registerSystemObservacaoUseCase,
            EventoAuditPublisher auditPublisher,
            CadastroEventoMetricsPublisher metricsPublisher,
            AprovacaoAcaoPayloadMapper payloadMapper,
            CacheManager cacheManager,
            EventoReservaIntegrationService eventoReservaIntegrationService) {
        this.eventoJpaRepository = eventoJpaRepository;
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.registerSystemObservacaoUseCase = registerSystemObservacaoUseCase;
        this.auditPublisher = auditPublisher;
        this.metricsPublisher = metricsPublisher;
        this.payloadMapper = payloadMapper;
        this.cacheManager = cacheManager;
        this.eventoReservaIntegrationService = eventoReservaIntegrationService;
    }

    /**
     * Tenta cancelar um evento. Retorna 200 OK para cancelamento imediato ou 202 ACCEPTED se aprovação for necessária.
     * <p>
     * Exemplo de uso:
     * useCase.execute(id, new CancelarEventoRequest("Personal reasons"));
     */
    @Transactional
    public ResultadoOperacaoEvento execute(UUID eventoId, CancelarEventoRequest request) {
        EventoEntity evento = findEntity(eventoId);
        EventoActorContext actorContext = actorContextResolver.resolveRequired();

        PapelGlobal atorGlobal = PapelGlobal.fromStoredValue(actorContext.role());
        PapelOrganizacional atorPapel = PapelOrganizacional.fromStoredValue(actorContext.role());
        if (atorGlobal == PapelGlobal.SECRETARIA_PAROQUIAL
                || atorPapel == PapelOrganizacional.SECRETARIO_CONSELHO
                || atorPapel == PapelOrganizacional.SECRETARIO_GRUPO) {
            if (request.motivo() == null || request.motivo().isBlank()) {
                throw new ViolacaoRegraNegocioException(
                        "Justificativa é obrigatória para operações submetidas pelo Secretário");
            }
        }

        validateCancellableStatus(evento);

        ModoRequisicaoCancelamento mode = authorizationService.resolveRequestMode(actorContext,
                evento.getGrupoParoquialResponsavelId());

        if (mode == ModoRequisicaoCancelamento.IMEDIATO) {
            return processImmediate(evento, request, actorContext);
        }

        return processAprovacao(evento, request, actorContext);
    }

    /**
     * Finaliza o processo de cancelamento de eventos já avaliados e aprovados, garantindo que todos os efeitos
     * colaterais, como registros de auditoria e notificações do sistema, sejam disparados.
     * <p>
     * Exemplo de uso:
     * {@code
     * useCase.executeApprovedCancellation(eventoId, "Aprovado pela coordenação", "admin", usuarioId);
     * }
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventoCanceladoResponse executeApprovedCancellation(UUID eventoId,
                                                               String motivo, String actor, UUID usuarioId) {

        EventoEntity evento = findEntity(eventoId);
        validateCancellableStatus(evento);
        metricsPublisher.publishCancellationFlow("EXECUTED_AFTER_APROVACAO", "SUCCESS");
        return applyCancellation(evento, motivo, actor, usuarioId, "executed-after-aprovacao");
    }

    private EventoEntity findEntity(UUID eventoId) {
        return eventoJpaRepository.findById(Objects.requireNonNull(eventoId))
                .orElseThrow(() -> new EventoNaoEncontradoException(eventoId));
    }

    private void validateCancellableStatus(EventoEntity evento) {
        EventoStatusInput currentStatus = EventoStatusInput.valueOf(evento.getStatus());
        if (currentStatus != EventoStatusInput.CONFIRMADO && currentStatus != EventoStatusInput.ADICIONADO_EXTRA) {
            throw new TransicaoStatusInvalidaException(evento.getStatus(),
                    EventoStatusInput.CANCELADO.name(),
                    "Only active events (CONFIRMADO or ADICIONADO_EXTRA) can be cancelled");
        }
    }

    private ResultadoOperacaoEvento processImmediate(EventoEntity evento,
                                                   CancelarEventoRequest request, EventoActorContext actorContext) {

        EventoCanceladoResponse response = applyCancellation(evento,
                request.motivo(), actorContext.actor(), actorContext.usuarioId(), "sucesso");

        metricsPublisher.publishCancellationFlow("IMMEDIATE", "SUCCESS");
        return new ResultadoOperacaoEvento.Sucesso(response, HttpStatus.OK);
    }

    private ResultadoOperacaoEvento processAprovacao(EventoEntity evento,
                                                   CancelarEventoRequest request, EventoActorContext actorContext) {

        CancelamentoPendenteResponse response = criarAprovacaoPendente(evento, request, actorContext);
        metricsPublisher.publishCancellationFlow("PENDENTE_CREATED", "PENDENTE");
        return new ResultadoOperacaoEvento.Pendente(response, HttpStatus.ACCEPTED);
    }

    private EventoCanceladoResponse applyCancellation(EventoEntity evento,
                                                      String motivo, String actor, UUID usuarioId, String auditResult) {

        evento.setStatus(EventoStatusInput.CANCELADO.name());
        evento.setCanceladoMotivo(motivo);
        EventoEntity saved = Objects.requireNonNull(eventoJpaRepository.save(evento));

        if (eventoReservaIntegrationService != null) {
            eventoReservaIntegrationService.cancelarReservasEventoEmCascata(
                    saved.getId(), saved.getTitulo(), usuarioId);
        }

        registerSystemObservacaoUseCase.execute(saved.getId(),
                TipoObservacaoInput.CANCELAMENTO, motivo, usuarioId, actor, "cancelamento");


        metricsPublisher.publishAdministrativeRework(false, true, false);
        metricsPublisher.recordDelecao("EVENTO", "DIRETA");
        auditPublisher.publish(actor, "cancelar", saved.getId().toString(), auditResult, Map.of(
                "eventoId", saved.getId(),
                "grupoParoquialId", saved.getGrupoParoquialResponsavelId(),
                "status", saved.getStatus(),
                "motivo", motivo,
                "tipoObservacao", TipoObservacaoInput.CANCELAMENTO.name()));

        evictCacheProjeto(saved.getProjetoId());

        return new EventoCanceladoResponse(saved.getId(),
                saved.getStatus(), saved.getCanceladoMotivo(), saved.getTitulo(),

                saved.getInicioUtc(), saved.getFimUtc(), saved.getGrupoParoquialResponsavelId());
    }

    private void evictCacheProjeto(UUID projetoId) {
        if (projetoId != null) {
            Optional.ofNullable(cacheManager.getCache(CacheConfig.PROJETO_RESUMO_CACHE))
                    .ifPresent(cache -> cache.evict(projetoId));
        }
    }

    private CancelamentoPendenteResponse criarAprovacaoPendente(EventoEntity evento,
                                                               CancelarEventoRequest request,
                                                               EventoActorContext actorContext) {

        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(UUID.randomUUID());
        aprovacao.setEventoId(evento.getId());
        aprovacao.setTipoSolicitacao(TipoSolicitacaoInput.CANCELAMENTO.name());
        aprovacao.setAprovadorPapel(AprovadorPapel.PAROCO);
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setSolicitanteId(actorContext.usuarioId().toString());
        aprovacao.setSolicitantePapel(actorContext.role());
        aprovacao.setSolicitanteTipoGrupo(TipoGrupo.fromStoredValue(actorContext.organizationType()));
        aprovacao.setMotivoCancelamentoSnapshot(request.motivo());
        aprovacao.setActionPayloadJson(payloadMapper.toJson(new AprovacaoAcaoPayload(
                null, evento.getId(), null, null, null, null, null, null, null, null,
                null, null, request.motivo() != null ? request.motivo() : "", null, null, null, null, null)));
        aprovacao.setCorrelationId(evento.getId().toString());
        aprovacaoJpaRepository.save(aprovacao);

        auditPublisher.publishCancellationPendente(actorContext.actor(),
                evento.getId().toString(), aprovacao.getId().toString(), request.motivo());


        return new CancelamentoPendenteResponse(aprovacao.getId(),
                aprovacao.getStatus().name(), evento.getId(), "APROVACAO_PENDENTE");

    }
}
