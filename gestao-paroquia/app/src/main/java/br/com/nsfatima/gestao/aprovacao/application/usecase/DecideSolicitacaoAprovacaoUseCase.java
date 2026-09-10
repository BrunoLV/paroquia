package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoMetricsPublisher;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.domain.event.AprovacaoDecisaoEvent;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoExecucaoFalhouException;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoNaoEncontradaException;
import br.com.nsfatima.gestao.aprovacao.domain.model.Aprovacao;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
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
 * Processa a decisão de uma solicitação de aprovação PENDENTE.
 *
 * <p>Toda a máquina de estados (guardas de PENDENTE/expiração, transição para APROVADA/REPROVADA e o
 * pós-processamento FALHA_EXECUCAO/execução concluída) vive no agregado {@link Aprovacao}; este caso
 * de uso apenas orquestra: autoriza o ator, dispara o comando de negócio, persiste e delega a ação
 * efetiva a uma {@link EstrategiaExecucaoAprovacao}.</p>
 */
@Service
public class DecideSolicitacaoAprovacaoUseCase {

    private static final String KEY_DECISION_STATUS = "statusDecisao";
    private static final String KEY_GRUPO_PAROQUIAL_ID = "grupoParoquialId";

    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final AprovacaoMapper aprovacaoMapper;
    private final EventoActorContextResolver actorContextResolver;
    private final ValidateAprovacaoUseCase validateAprovacaoUseCase;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;
    private final AprovacaoAuditPublisher auditPublisher;
    private final AprovacaoMetricsPublisher metricsPublisher;
    private final List<EstrategiaExecucaoAprovacao> strategies;
    private final ApplicationEventPublisher eventPublisher;

    public DecideSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            AprovacaoMapper aprovacaoMapper,
            EventoActorContextResolver actorContextResolver,
            ValidateAprovacaoUseCase validateAprovacaoUseCase,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper,
            AprovacaoAuditPublisher auditPublisher,
            AprovacaoMetricsPublisher metricsPublisher,
            List<EstrategiaExecucaoAprovacao> strategies,
            ApplicationEventPublisher eventPublisher) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.aprovacaoMapper = aprovacaoMapper;
        this.actorContextResolver = actorContextResolver;
        this.validateAprovacaoUseCase = validateAprovacaoUseCase;
        this.aprovacaoAcaoPayloadMapper = aprovacaoAcaoPayloadMapper;
        this.auditPublisher = auditPublisher;
        this.metricsPublisher = metricsPublisher;
        this.strategies = strategies;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(noRollbackFor = AprovacaoExecucaoFalhouException.class)
    public AprovacaoDecisionResponse decide(UUID aprovacaoId, AprovacaoDecisionRequest request) {
        AprovacaoEntity entity = aprovacaoJpaRepository.findById(aprovacaoId)
                .orElseThrow(() -> new AprovacaoNaoEncontradaException("Aprovação não encontrada: " + aprovacaoId));
        Aprovacao aprovacao = aprovacaoMapper.toDomain(entity);
        aprovacao.assegurarDecidivel(inicioAcao(entity), Instant.now());

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        validateAprovacaoUseCase.validateAprovacaoDecisionRole(actorContext.role(), actorContext.organizationType());

        String aprovadorId = actorContext.usuarioId() != null ? actorContext.usuarioId().toString() : "system";
        if (request.status() == AprovacaoStatus.REPROVADA) {
            aprovacao.reprovar(aprovadorId, actorContext.role(), request.observacao(), Instant.now());
        } else {
            aprovacao.aprovar(aprovadorId, actorContext.role(), request.observacao(), Instant.now());
        }
        aprovacaoMapper.aplicar(aprovacao, entity);

        if (request.status() == AprovacaoStatus.REPROVADA) {
            return processRejection(aprovacao, entity, actorContext);
        }
        return processAprovacao(aprovacao, entity, actorContext);
    }

    private Instant inicioAcao(AprovacaoEntity entity) {
        try {
            AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(entity.getActionPayloadJson());
            return payload == null ? null : payload.inicio();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private AprovacaoDecisionResponse processRejection(Aprovacao aprovacao, AprovacaoEntity entity,
                                                      EventoActorContext actorContext) {
        aprovacaoJpaRepository.save(entity);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(KEY_DECISION_STATUS, AprovacaoStatus.REPROVADA.name());
        if (actorContext.organizationId() != null) {
            metadata.put(KEY_GRUPO_PAROQUIAL_ID, actorContext.organizationId());
        }

        if (auditPublisher != null) {
            auditPublisher.publishAprovacaoDecision(actorContext.actor(), entity, "reprovado", metadata);
        }
        if (metricsPublisher != null) {
            metricsPublisher.publishAprovacaoFlow(
                    aprovacao.getTipoSolicitacao() == null ? "UNKNOWN" : aprovacao.getTipoSolicitacao(), "REJECTED");
        }

        publishDecisionEvent(entity, null);

        TipoSolicitacaoInput tipo = TipoSolicitacaoInput.fromJson(aprovacao.getTipoSolicitacao());
        EstrategiaExecucaoAprovacao matchingStrategy = strategies.stream()
                .filter(s -> s.supports(tipo))
                .findFirst()
                .orElse(null);
        if (matchingStrategy != null) {
            matchingStrategy.onRejected(entity);
        }
        String currentStatus = matchingStrategy == null ? null : matchingStrategy.fetchResourceStatus(entity);

        return new AprovacaoDecisionResponse(aprovacao.getId(), aprovacao.getStatus().name(),
                new AprovacaoDecisionResponse.ActionExecution(
                        "REJECTED", aprovacao.getEventoId(), currentStatus, null));
    }

    private AprovacaoDecisionResponse processAprovacao(Aprovacao aprovacao, AprovacaoEntity entity,
                                                      EventoActorContext actorContext) {
        TipoSolicitacaoInput tipo = TipoSolicitacaoInput.fromJson(aprovacao.getTipoSolicitacao());
        EstrategiaExecucaoAprovacao strategy = strategies.stream()
                .filter(s -> s.supports(tipo))
                .findFirst()
                .orElseThrow(() -> new AprovacaoExecucaoFalhouException(
                        "Unsupported aprovacao type: " + aprovacao.getTipoSolicitacao()));

        try {
            AprovacaoDecisionResponse.ActionExecution execution = strategy.execute(entity, actorContext);
            finalizeSuccessfulExecution(aprovacao, entity, execution, actorContext, tipo);
            return new AprovacaoDecisionResponse(aprovacao.getId(), aprovacao.getStatus().name(), execution);
        } catch (RuntimeException ex) {
            handleExecutionFailure(aprovacao, entity, ex, actorContext, tipo);
            throw new AprovacaoExecucaoFalhouException("Falha na execução: " + ex.getMessage());
        }
    }

    private void finalizeSuccessfulExecution(Aprovacao aprovacao, AprovacaoEntity entity,
                                             AprovacaoDecisionResponse.ActionExecution execution,
                                             EventoActorContext actorContext, TipoSolicitacaoInput tipo) {
        Instant executedAt = Instant.now();
        aprovacao.registrarExecucaoConcluida(executedAt);
        aprovacaoMapper.aplicar(aprovacao, entity);
        aprovacaoJpaRepository.save(entity);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventoId", execution.eventoId() == null ? "N/A" : execution.eventoId().toString());
        metadata.put(KEY_DECISION_STATUS, AprovacaoStatus.APROVADA.name());
        if (actorContext.organizationId() != null) {
            metadata.put(KEY_GRUPO_PAROQUIAL_ID, actorContext.organizationId());
        }

        if (auditPublisher != null) {
            auditPublisher.publishAprovacaoDecision(actorContext.actor(), entity, "executado", metadata);
        }
        if (metricsPublisher != null) {
            metricsPublisher.publishAprovacaoFlow(tipo.name(), "EXECUTED");
            if (aprovacao.getDecididoEmUtc() != null) {
                metricsPublisher.publishAprovacaoExecutionLatency(tipo.name(),
                        Duration.between(aprovacao.getDecididoEmUtc(), executedAt));
            }
        }
        publishDecisionEvent(entity, null);
    }

    private void handleExecutionFailure(Aprovacao aprovacao, AprovacaoEntity entity, RuntimeException ex,
                                        EventoActorContext actorContext, TipoSolicitacaoInput tipo) {
        aprovacao.registrarFalhaExecucao(ex.getMessage());
        aprovacaoMapper.aplicar(aprovacao, entity);
        aprovacaoJpaRepository.save(entity);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", ex.getClass().getSimpleName());
        metadata.put(KEY_DECISION_STATUS, AprovacaoStatus.APROVADA.name());
        if (actorContext.organizationId() != null) {
            metadata.put(KEY_GRUPO_PAROQUIAL_ID, actorContext.organizationId());
        }

        if (auditPublisher != null) {
            auditPublisher.publishAprovacaoDecision(actorContext.actor(), entity, "falhou", metadata);
        }
        if (metricsPublisher != null) {
            metricsPublisher.publishAprovacaoFlow(tipo.name(), "FAILED");
        }
        publishDecisionEvent(entity, ex.getMessage());
    }

    private void publishDecisionEvent(AprovacaoEntity entity, String errorMessage) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new AprovacaoDecisaoEvent(
                    entity.getId(), entity.getEventoId(), entity.getSolicitanteId(),
                    entity.getStatus(), entity.getAprovadorId(), errorMessage));
        }
    }
}
