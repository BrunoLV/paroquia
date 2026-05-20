package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core use case for processing decisions on pending approval requests.
 * Refactored to be more generic and decoupled via interfaces.
 */
@Service
public class DecideSolicitacaoAprovacaoUseCase {

    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final ValidateAprovacaoUseCase validateAprovacaoUseCase;
    private final ApprovalActionPayloadMapper approvalActionPayloadMapper;
    private final ApprovalAuditPublisher auditPublisher;
    private final ApprovalMetricsPublisher metricsPublisher;
    private final List<ApprovalExecutionStrategy> strategies;
    private final ApplicationEventPublisher eventPublisher;

    public DecideSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoActorContextResolver actorContextResolver,
            ValidateAprovacaoUseCase validateAprovacaoUseCase,
            ApprovalActionPayloadMapper approvalActionPayloadMapper,
            ApprovalAuditPublisher auditPublisher,
            ApprovalMetricsPublisher metricsPublisher,
            List<ApprovalExecutionStrategy> strategies,
            ApplicationEventPublisher eventPublisher) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.actorContextResolver = actorContextResolver;
        this.validateAprovacaoUseCase = validateAprovacaoUseCase;
        this.approvalActionPayloadMapper = approvalActionPayloadMapper;
        this.auditPublisher = auditPublisher;
        this.metricsPublisher = metricsPublisher;
        this.strategies = strategies;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Orchestrates the final processing of an approval request, ensuring that only authorized personnel can make decisions and that the resulting actions are executed atomically.
     * 
     * Usage Example:
     * {@code
     * useCase.decide(aprovacaoId, new AprovacaoDecisionRequest(AprovacaoStatus.APROVADA, "Ready to proceed"));
     * }
     */
    @Transactional(noRollbackFor = ApprovalExecutionFailedException.class)
    public AprovacaoDecisionResponse decide(UUID aprovacaoId, AprovacaoDecisionRequest request) {
        AprovacaoEntity aprovacao = findAndValidatePending(aprovacaoId);
        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        
        validateAprovacaoUseCase.validateApprovalDecisionRole(
                actorContext.role(),
                actorContext.organizationType());

        applyDecisionState(aprovacao, request, actorContext);

        if (request.status() == AprovacaoStatus.REPROVADA) {
            return processRejection(aprovacao, actorContext);
        }

        return processApproval(aprovacao, actorContext);
    }

    private AprovacaoEntity findAndValidatePending(UUID aprovacaoId) {
        AprovacaoEntity aprovacao = aprovacaoJpaRepository.findById(aprovacaoId)
                .orElseThrow(() -> new ApprovalNotFoundException("Approval not found: " + aprovacaoId));

        if (aprovacao.getStatusEnum() != AprovacaoStatus.PENDENTE) {
            throw new ApprovalAlreadyDecidedException("Approval already decided: " + aprovacaoId);
        }

        if (isExpired(aprovacao)) {
            Instant start = approvalActionPayloadMapper.toPayload(aprovacao.getActionPayloadJson()).inicio();
            throw new ApprovalExecutionFailedException("Approval request has expired (event already started at %s)".formatted(start));
        }
        return aprovacao;
    }

    private void applyDecisionState(AprovacaoEntity aprovacao, AprovacaoDecisionRequest request, EventoActorContext actorContext) {
        aprovacao.setStatus(request.status());
        aprovacao.setDecididoEmUtc(Instant.now());
        aprovacao.setDecisionObservacao(request.observacao());
        aprovacao.setAprovadorId(actorContext.usuarioId() != null ? actorContext.usuarioId().toString() : "system");
        aprovacao.setAprovadorPapel(actorContext.role());
    }

    private AprovacaoDecisionResponse processRejection(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        aprovacaoJpaRepository.save(aprovacao);
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("decisionStatus", AprovacaoStatus.REPROVADA.name());
        if (actorContext.organizationId() != null) {
            metadata.put("organizacaoId", actorContext.organizationId());
        }

        if (auditPublisher != null) {
            auditPublisher.publishApprovalDecision(actorContext.actor(), aprovacao, "rejected", metadata);
        }
        
        if (metricsPublisher != null) {
            metricsPublisher.publishApprovalFlow(
                aprovacao.getTipoSolicitacao() == null ? "UNKNOWN" : aprovacao.getTipoSolicitacao(),
                "REJECTED");
        }

        publishDecisionEvent(aprovacao, null);

        TipoSolicitacaoInput tipo = TipoSolicitacaoInput.fromJson(aprovacao.getTipoSolicitacao());
        String currentStatus = strategies.stream()
                .filter(s -> s.supports(tipo))
                .findFirst()
                .map(s -> s.fetchResourceStatus(aprovacao))
                .orElse(null);

        return new AprovacaoDecisionResponse(aprovacao.getId(), aprovacao.getStatusEnum().name(),
                new AprovacaoDecisionResponse.ActionExecution("REJECTED", aprovacao.getEventoId(), currentStatus, null));
    }

    private AprovacaoDecisionResponse processApproval(AprovacaoEntity aprovacao, EventoActorContext actorContext) {
        TipoSolicitacaoInput tipo = TipoSolicitacaoInput.fromJson(aprovacao.getTipoSolicitacao());
        ApprovalExecutionStrategy strategy = strategies.stream()
                .filter(s -> s.supports(tipo))
                .findFirst()
                .orElseThrow(() -> new ApprovalExecutionFailedException(
                        "Unsupported approval type: " + aprovacao.getTipoSolicitacao()));

        try {
            AprovacaoDecisionResponse.ActionExecution execution = strategy.execute(aprovacao, actorContext);
            finalizeSuccessfulExecution(aprovacao, execution, actorContext, tipo);
            return new AprovacaoDecisionResponse(aprovacao.getId(), aprovacao.getStatusEnum().name(), execution);
        } catch (RuntimeException ex) {
            handleExecutionFailure(aprovacao, ex, actorContext, tipo);
            throw new ApprovalExecutionFailedException("Execution failed: " + ex.getMessage());
        }
    }

    private void finalizeSuccessfulExecution(AprovacaoEntity aprovacao, AprovacaoDecisionResponse.ActionExecution execution, 
                                          EventoActorContext actorContext, TipoSolicitacaoInput tipo) {
        Instant executedAt = Instant.now();
        aprovacao.setExecutadoEmUtc(executedAt);
        aprovacaoJpaRepository.save(aprovacao);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventoId", execution.eventoId() == null ? "N/A" : execution.eventoId().toString());
        metadata.put("decisionStatus", AprovacaoStatus.APROVADA.name());
        if (actorContext.organizationId() != null) {
            metadata.put("organizacaoId", actorContext.organizationId());
        }

        if (auditPublisher != null) {
            auditPublisher.publishApprovalDecision(actorContext.actor(), aprovacao, "executed", metadata);
        }

        if (metricsPublisher != null) {
            metricsPublisher.publishApprovalFlow(tipo.name(), "EXECUTED");
            if (aprovacao.getDecididoEmUtc() != null) {
                metricsPublisher.publishApprovalExecutionLatency(tipo.name(), 
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
        metadata.put("decisionStatus", AprovacaoStatus.APROVADA.name());
        if (actorContext.organizationId() != null) {
            metadata.put("organizacaoId", actorContext.organizationId());
        }

        if (auditPublisher != null) {
            auditPublisher.publishApprovalDecision(actorContext.actor(), aprovacao, "failed", metadata);
        }
        
        if (metricsPublisher != null) {
            metricsPublisher.publishApprovalFlow(tipo.name(), "FAILED");
        }
        publishDecisionEvent(aprovacao, ex.getMessage());
    }

    private void publishDecisionEvent(AprovacaoEntity aprovacao, String errorMessage) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new ApprovalDecisionEvent(
                    aprovacao.getId(), aprovacao.getEventoId(), aprovacao.getSolicitanteId(),
                    aprovacao.getStatusEnum(), aprovacao.getAprovadorId(), errorMessage));
        }
    }

    private boolean isExpired(AprovacaoEntity aprovacao) {
        try {
            ApprovalActionPayload payload = approvalActionPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
            return payload.inicio() != null && payload.inicio().isBefore(Instant.now());
        } catch (RuntimeException ex) {
            return false;
        }
    }
}
