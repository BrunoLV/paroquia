package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.DecisaoAprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoMetricsPublisher;
import br.com.nsfatima.gestao.aprovacao.application.service.AprovacaoRecalculoConflitoService;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.domain.event.AprovacaoDecisaoEvent;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoExecucaoFalhouException;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoNaoEncontradaException;
import br.com.nsfatima.gestao.aprovacao.domain.model.Aprovacao;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.domain.model.TipoFalhaAprovacao;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.exception.ViolacaoRegraNegocioException;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Caso de uso para reexecutar a ação de negócio de uma solicitação de aprovação que se encontra no estado
 * {@link AprovacaoStatus#FALHA_EXECUCAO}.
 *
 * <p>Regra de Governança: Somente solicitações cuja falha foi de natureza
 * {@link TipoFalhaAprovacao#SISTEMICA_APLICACAO} admitem reexecução direta. Solicitações com falhas de negócio
 * ou cadastrais (ex.: conflitos de espaço físico ou ativos) são imutáveis e exigem abertura de um novo cadastro
 * com dados corrigidos.</p>
 */
@Service
public class ReexecutarAprovacaoUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReexecutarAprovacaoUseCase.class);

    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final AprovacaoMapper aprovacaoMapper;
    private final EventoActorContextResolver actorContextResolver;
    private final ValidarAprovacaoUseCase validarAprovacaoUseCase;
    private final AprovacaoAuditPublisher auditPublisher;
    private final AprovacaoMetricsPublisher metricsPublisher;
    private final List<EstrategiaExecucaoAprovacao> strategies;
    private final ApplicationEventPublisher eventPublisher;
    private final AprovacaoRecalculoConflitoService recalculoConflitoService;

    @Autowired
    public ReexecutarAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            AprovacaoMapper aprovacaoMapper,
            EventoActorContextResolver actorContextResolver,
            ValidarAprovacaoUseCase validarAprovacaoUseCase,
            AprovacaoAuditPublisher auditPublisher,
            AprovacaoMetricsPublisher metricsPublisher,
            List<EstrategiaExecucaoAprovacao> strategies,
            ApplicationEventPublisher eventPublisher,
            @Autowired(required = false) AprovacaoRecalculoConflitoService recalculoConflitoService) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.aprovacaoMapper = aprovacaoMapper;
        this.actorContextResolver = actorContextResolver;
        this.validarAprovacaoUseCase = validarAprovacaoUseCase;
        this.auditPublisher = auditPublisher;
        this.metricsPublisher = metricsPublisher;
        this.strategies = strategies;
        this.eventPublisher = eventPublisher;
        this.recalculoConflitoService = recalculoConflitoService;
    }

    @Transactional(noRollbackFor = AprovacaoExecucaoFalhouException.class)
    public DecisaoAprovacaoResponse reexecutar(UUID aprovacaoId) {
        AprovacaoEntity entity = aprovacaoJpaRepository.findById(aprovacaoId)
                .orElseThrow(() -> new AprovacaoNaoEncontradaException("Aprovação não encontrada: " + aprovacaoId));

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        validarAprovacaoUseCase.validarPapelDecisaoAprovacao(actorContext.role(), actorContext.organizationType());

        Aprovacao aprovacao = aprovacaoMapper.toDomain(entity);
        aprovacao.reexecutar(Instant.now());

        TipoSolicitacaoInput tipo = TipoSolicitacaoInput.fromJson(aprovacao.getTipoSolicitacao());
        EstrategiaExecucaoAprovacao strategy = strategies.stream()
                .filter(s -> s.supports(tipo))
                .findFirst()
                .orElseThrow(() -> new AprovacaoExecucaoFalhouException(
                        "Unsupported aprovacao type: " + aprovacao.getTipoSolicitacao()));

        try {
            LOGGER.info("Iniciando reexecução da aprovação id={}, tipo={}", aprovacaoId, tipo);
            DecisaoAprovacaoResponse.ActionExecution execution = strategy.execute(entity, actorContext);

            Instant executedAt = Instant.now();
            aprovacao.registrarExecucaoConcluida(executedAt);
            aprovacaoMapper.aplicar(aprovacao, entity);
            aprovacaoJpaRepository.save(entity);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("eventoId", execution.eventoId() == null ? "N/A" : execution.eventoId().toString());
            metadata.put("statusDecisao", AprovacaoStatus.APROVADA.name());
            metadata.put("reexecutado", Boolean.TRUE);

            if (auditPublisher != null) {
                auditPublisher.publishAprovacaoDecision(actorContext.actor(), entity, "reexecutado_sucesso", metadata);
            }
            if (metricsPublisher != null) {
                metricsPublisher.publishAprovacaoFlow(tipo.name(), "REEXECUTED_SUCCESS");
                metricsPublisher.recordAprovacao(tipo.name());
            }
            if (eventPublisher != null) {
                eventPublisher.publishEvent(new AprovacaoDecisaoEvent(
                        entity.getId(), entity.getEventoId(), entity.getSolicitanteId(),
                        entity.getStatus(), entity.getAprovadorId(), null));
            }
            if (recalculoConflitoService != null) {
                recalculoConflitoService.recalcularConflitosPendentes(entity.getId());
            }

            return new DecisaoAprovacaoResponse(aprovacao.getId(), aprovacao.getStatus().name(), execution);
        } catch (RuntimeException ex) {
            boolean isNegocio = isFalhaNegocio(ex);
            TipoFalhaAprovacao tipoFalha =
                    isNegocio ? TipoFalhaAprovacao.NEGOCIO_CADASTRAL : TipoFalhaAprovacao.SISTEMICA_APLICACAO;
            String detalhe = extrairDetalheErro(ex);

            if (isNegocio) {
                LOGGER.warn("Reexecução da aprovação id={} falhou por regra cadastral/de negócio: {}",
                        entity.getId(), ex.getMessage());
            } else {
                LOGGER.error("Falha sistêmica durante reexecução da aprovação id={}, tipo={}",
                        entity.getId(), tipo, ex);
            }

            aprovacao.registrarFalhaExecucao(tipoFalha, ex.getMessage(), detalhe);
            aprovacaoMapper.aplicar(aprovacao, entity);
            aprovacaoJpaRepository.save(entity);

            if (auditPublisher != null) {
                auditPublisher.publishAprovacaoDecision(actorContext.actor(), entity, "reexecutado_falha",
                        Map.of("error", ex.getClass().getSimpleName(), "tipoFalha", tipoFalha.name()));
            }
            if (metricsPublisher != null) {
                metricsPublisher.publishAprovacaoFlow(tipo.name(), "REEXECUTED_FAILED");
                metricsPublisher.recordFalhaExecucao(tipo.name(), tipoFalha.name());
            }
            if (eventPublisher != null) {
                eventPublisher.publishEvent(new AprovacaoDecisaoEvent(
                        entity.getId(), entity.getEventoId(), entity.getSolicitanteId(),
                        entity.getStatus(), entity.getAprovadorId(), ex.getMessage()));
            }

            throw new AprovacaoExecucaoFalhouException("Falha na reexecução: " + ex.getMessage());
        }
    }

    private boolean isFalhaNegocio(RuntimeException ex) {
        if (ex instanceof ViolacaoRegraNegocioException) {
            return true;
        }
        String className = ex.getClass().getSimpleName();
        if (className.contains("Conflito") || className.contains("Negocio") || className.contains("Invalido")) {
            return true;
        }
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
        return msg.contains("conflito") || msg.contains("expirad")
                || msg.contains("inválid") || msg.contains("invalido");
    }

    private String extrairDetalheErro(RuntimeException ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.getClass().getName()).append(": ").append(ex.getMessage());
        if (ex.getCause() != null) {
            sb.append(" | Causa: ").append(ex.getCause().getClass().getName())
                    .append(": ").append(ex.getCause().getMessage());
        }
        return sb.toString();
    }
}
