package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoMetricsPublisher;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.domain.event.AprovacaoDecisaoEvent;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoJaDecididaException;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoExecucaoFalhouException;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoNaoEncontradaException;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Core use case for processing decisions on pendente aprovacao requests.
 * Refactored to be more generic and decoupled via interfaces.
 */
@Service
public class DecideSolicitacaoAprovacaoUseCase {

    private static final String KEY_DECISION_STATUS = "statusDecisao";
    private static final String KEY_ORGANIZACAO_ID = "organizacaoId";
    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final ValidateAprovacaoUseCase validateAprovacaoUseCase;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;
    private final AprovacaoAuditPublisher auditPublisher;
    private final AprovacaoMetricsPublisher metricsPublisher;
    private final List<EstrategiaExecucaoAprovacao> strategies;
    private final ApplicationEventPublisher eventPublisher;

    public DecideSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoActorContextResolver actorContextResolver,
            ValidateAprovacaoUseCase validateAprovacaoUseCase,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper,
            AprovacaoAuditPublisher auditPublisher,
            AprovacaoMetricsPublisher metricsPublisher,
            List<EstrategiaExecucaoAprovacao> strategies,
            ApplicationEventPublisher eventPublisher) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.actorContextResolver = actorContextResolver;
        this.validateAprovacaoUseCase = validateAprovacaoUseCase;
        this.aprovacaoAcaoPayloadMapper = aprovacaoAcaoPayloadMapper;
        this.auditPublisher = auditPublisher;
        this.metricsPublisher = metricsPublisher;
        this.strategies = strategies;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Orchestrates the final processing of an aprovacao request, ensuring that only authorized personnel can make
     * decisions and that the resulting actions are executed atomically.
     * <p>
     * Usage Example:
     * {@code
     * useCase.decide(aprovacaoId, new AprovacaoDecisionRequest(AprovacaoStatus.APROVADA, "Ready to proceed"));
     * }
     */
    @Transactional(noRollbackFor = AprovacaoExecucaoFalhouException.class)
    public AprovacaoDecisionResponse decide(UUID aprovacaoId, AprovacaoDecisionRequest request) {
        AprovacaoEntity aprovacao = findAndValidatePendente(aprovacaoId);
        EventoActorContext actorContext = actorContextResolver.resolveRequired();

        validateAprovacaoUseCase.validateAprovacaoDecisionRole(
                actorContext.role(),
                actorContext.organizationType());

        applyDecisionState(aprovacao, request, actorContext);

        if (request.status() == AprovacaoStatus.REPROVADA) {
            return processRejection(aprovacao, actorContext);
        }

        return processAprovacao(aprovacao, actorContext);
    }

    private AprovacaoEntity findAndValidatePendente(UUID aprovacaoId) {
        AprovacaoEntity aprovacao = aprovacaoJpaRepository.findById(aprovacaoId)
                .orElseThrow(() -> new AprovacaoNaoEncontradaException("Aprovação não encontrada: " + aprovacaoId));

        if (aprovacao.getStatus() != AprovacaoStatus.PENDENTE) {
            throw new AprovacaoJaDecididaException("Aprovação já decidida: " + aprovacaoId);
        }

        if (isExpired(aprovacao)) {
            Instant start = aprovacaoAcaoPayloadMapper.toPayload(aprovacao.getActionPayloadJson()).inicio();
            throw new AprovacaoExecucaoFalhouException(
                    "Aprovacao request has expired (event already started at %s)".formatted(start));
        }
        return aprovacao;
    }

    private void applyDecisionState(AprovacaoEntity aprovacao,
                                    AprovacaoDecisionRequest request, EventoActorContext actorContext) {

        aprovacao.setStatus(request.status());
        aprovacao.setDecididoEmUtc(Instant.now());
        aprovacao.setDecisionObservacao(request.observacao());
        aprovacao.setAprovadorId(actorContext.usuarioId() != null ? actorContext.usuarioId().toString() : "system");
        aprovacao.setAprovadorPapel(actorContext.role());
    }

    private AprovacaoDecisionResponse processRejection(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        aprovacaoJpaRepository.save(aprovacao);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(KEY_DECISION_STATUS, AprovacaoStatus.REPROVADA.name());
        if (actorContext.organizationId() != null) {
            metadata.put(KEY_ORGANIZACAO_ID, actorContext.organizationId());
        }

        if (auditPublisher != null) {
            auditPublisher.publishAprovacaoDecision(actorContext.actor(), aprovacao, "reprovado", metadata);
        }

        if (metricsPublisher != null) {
            metricsPublisher.publishAprovacaoFlow(
                    aprovacao.getTipoSolicitacao() == null ? "UNKNOWN" : aprovacao.getTipoSolicitacao(),
                    "REJECTED");
        }

        publishDecisionEvent(aprovacao, null);

        TipoSolicitacaoInput tipo = TipoSolicitacaoInput.fromJson(aprovacao.getTipoSolicitacao());
        EstrategiaExecucaoAprovacao matchingStrategy = strategies.stream()
                .filter(s -> s.supports(tipo))
                .findFirst()
                .orElse(null);
        if (matchingStrategy != null) {
            matchingStrategy.onRejected(aprovacao);
        }
        String currentStatus = matchingStrategy == null ? null : matchingStrategy.fetchResourceStatus(aprovacao);

        return new AprovacaoDecisionResponse(aprovacao.getId(), aprovacao.getStatus().name(),
                new AprovacaoDecisionResponse.ActionExecution("REJECTED",
                        aprovacao.getEventoId(), currentStatus, null));

    }

    private AprovacaoDecisionResponse processAprovacao(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        TipoSolicitacaoInput tipo = TipoSolicitacaoInput.fromJson(aprovacao.getTipoSolicitacao());
        EstrategiaExecucaoAprovacao strategy = strategies.stream()
                .filter(s -> s.supports(tipo))
                .findFirst()
                .orElseThrow(() -> new AprovacaoExecucaoFalhouException(
                        "Unsupported aprovacao type: " + aprovacao.getTipoSolicitacao()));

        try {
            AprovacaoDecisionResponse.ActionExecution execution = strategy.execute(aprovacao, actorContext);
            finalizeSuccessfulExecution(aprovacao, execution, actorContext, tipo);
            return new AprovacaoDecisionResponse(aprovacao.getId(), aprovacao.getStatus().name(), execution);
        } catch (RuntimeException ex) {
            handleExecutionFailure(aprovacao, ex, actorContext, tipo);
            throw new AprovacaoExecucaoFalhouException("Falha na execução: " + ex.getMessage());
        }
    }

    private void finalizeSuccessfulExecution(AprovacaoEntity aprovacao,
                                             AprovacaoDecisionResponse.ActionExecution execution,

                                             EventoActorContext actorContext, TipoSolicitacaoInput tipo) {
        Instant executedAt = Instant.now();
        aprovacao.setExecutadoEmUtc(executedAt);
        aprovacaoJpaRepository.save(aprovacao);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventoId", execution.eventoId() == null ? "N/A" : execution.eventoId().toString());
        metadata.put(KEY_DECISION_STATUS, AprovacaoStatus.APROVADA.name());
        if (actorContext.organizationId() != null) {
            metadata.put(KEY_ORGANIZACAO_ID, actorContext.organizationId());
        }

        if (auditPublisher != null) {
            auditPublisher.publishAprovacaoDecision(actorContext.actor(), aprovacao, "executado", metadata);
        }

        if (metricsPublisher != null) {
            metricsPublisher.publishAprovacaoFlow(tipo.name(), "EXECUTED");
            if (aprovacao.getDecididoEmUtc() != null) {
                metricsPublisher.publishAprovacaoExecutionLatency(tipo.name(),
                        Duration.between(aprovacao.getDecididoEmUtc(), executedAt));
            }
        }
        publishDecisionEvent(aprovacao, null);
    }

    private void handleExecutionFailure(AprovacaoEntity aprovacao, RuntimeException ex,
                                        EventoActorContext actorContext, TipoSolicitacaoInput tipo) {
        aprovacao.setStatus(AprovacaoStatus.FALHA_EXECUCAO);
        aprovacao.setMensagemErroExecucao(ex.getMessage());
        aprovacaoJpaRepository.save(aprovacao);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", ex.getClass().getSimpleName());
        metadata.put(KEY_DECISION_STATUS, AprovacaoStatus.APROVADA.name());
        if (actorContext.organizationId() != null) {
            metadata.put(KEY_ORGANIZACAO_ID, actorContext.organizationId());
        }

        if (auditPublisher != null) {
            auditPublisher.publishAprovacaoDecision(actorContext.actor(), aprovacao, "falhou", metadata);
        }

        if (metricsPublisher != null) {
            metricsPublisher.publishAprovacaoFlow(tipo.name(), "FAILED");
        }
        publishDecisionEvent(aprovacao, ex.getMessage());
    }

    private void publishDecisionEvent(AprovacaoEntity aprovacao, String errorMessage) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new AprovacaoDecisaoEvent(
                    aprovacao.getId(), aprovacao.getEventoId(), aprovacao.getSolicitanteId(),
                    aprovacao.getStatus(), aprovacao.getAprovadorId(), errorMessage));
        }
    }

    private boolean isExpired(AprovacaoEntity aprovacao) {
        try {
            AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
            return payload.inicio() != null && payload.inicio().isBefore(Instant.now());
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
