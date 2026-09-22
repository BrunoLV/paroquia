package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.DecidirAprovacaoRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.DecisaoAprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoMetricsPublisher;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.domain.event.AprovacaoDecisaoEvent;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoExecucaoFalhouException;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoNaoEncontradaException;
import br.com.nsfatima.gestao.calendario.domain.exception.ViolacaoRegraNegocioException;
import br.com.nsfatima.gestao.aprovacao.domain.model.Aprovacao;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.domain.model.TipoFalhaAprovacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.aprovacao.application.service.AprovacaoRecalculoConflitoService;
import br.com.nsfatima.gestao.calendario.application.dto.ConflitoEventoItemDto;
import br.com.nsfatima.gestao.calendario.application.service.DetectorConflitoEventoService;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DecidirSolicitacaoAprovacaoUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DecidirSolicitacaoAprovacaoUseCase.class);
    private static final String KEY_DECISION_STATUS = "statusDecisao";
    private static final String KEY_GRUPO_PAROQUIAL_ID = "grupoParoquialId";

    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final AprovacaoMapper aprovacaoMapper;
    private final EventoActorContextResolver actorContextResolver;
    private final ValidarAprovacaoUseCase validarAprovacaoUseCase;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;
    private final AprovacaoAuditPublisher auditPublisher;
    private final AprovacaoMetricsPublisher metricsPublisher;
    private final List<EstrategiaExecucaoAprovacao> strategies;
    private final ApplicationEventPublisher eventPublisher;
    private final DetectorConflitoEventoService detectorConflitoEventoService;
    private final AprovacaoRecalculoConflitoService recalculoConflitoService;

    @Autowired
    public DecidirSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            AprovacaoMapper aprovacaoMapper,
            EventoActorContextResolver actorContextResolver,
            ValidarAprovacaoUseCase validarAprovacaoUseCase,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper,
            AprovacaoAuditPublisher auditPublisher,
            AprovacaoMetricsPublisher metricsPublisher,
            List<EstrategiaExecucaoAprovacao> strategies,
            ApplicationEventPublisher eventPublisher,
            @Autowired(required = false) DetectorConflitoEventoService detectorConflitoEventoService,
            @Autowired(required = false) AprovacaoRecalculoConflitoService recalculoConflitoService) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.aprovacaoMapper = aprovacaoMapper;
        this.actorContextResolver = actorContextResolver;
        this.validarAprovacaoUseCase = validarAprovacaoUseCase;
        this.aprovacaoAcaoPayloadMapper = aprovacaoAcaoPayloadMapper;
        this.auditPublisher = auditPublisher;
        this.metricsPublisher = metricsPublisher;
        this.strategies = strategies;
        this.eventPublisher = eventPublisher;
        this.detectorConflitoEventoService = detectorConflitoEventoService;
        this.recalculoConflitoService = recalculoConflitoService;
    }

    public DecidirSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            AprovacaoMapper aprovacaoMapper,
            EventoActorContextResolver actorContextResolver,
            ValidarAprovacaoUseCase validarAprovacaoUseCase,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper,
            AprovacaoAuditPublisher auditPublisher,
            AprovacaoMetricsPublisher metricsPublisher,
            List<EstrategiaExecucaoAprovacao> strategies,
            ApplicationEventPublisher eventPublisher) {
        this(aprovacaoJpaRepository, aprovacaoMapper, actorContextResolver, validarAprovacaoUseCase,
                aprovacaoAcaoPayloadMapper, auditPublisher, metricsPublisher, strategies, eventPublisher, null, null);
    }

    @Transactional(noRollbackFor = AprovacaoExecucaoFalhouException.class)
    public DecisaoAprovacaoResponse decidir(UUID aprovacaoId, DecidirAprovacaoRequest request) {
        AprovacaoEntity entity = aprovacaoJpaRepository.findById(aprovacaoId)
                .orElseThrow(() -> new AprovacaoNaoEncontradaException("Aprovação não encontrada: " + aprovacaoId));
        Aprovacao aprovacao = aprovacaoMapper.toDomain(entity);
        aprovacao.assegurarDecidivel(inicioAcao(entity), Instant.now());

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        validarAprovacaoUseCase.validarPapelDecisaoAprovacao(actorContext.role(), actorContext.organizationType());

        String aprovadorId = actorContext.usuarioId() != null ? actorContext.usuarioId().toString() : "system";

        boolean isMasterRecorrente = entity.getAprovacaoPaiId() == null
                && aprovacaoJpaRepository.countByAprovacaoPaiId(entity.getId()) > 0;

        if (request.status() == AprovacaoStatus.APROVADA) {
            if (isMasterRecorrente) {
                List<AprovacaoEntity> filhasPendentes = aprovacaoJpaRepository.findByAprovacaoPaiIdAndStatus(
                        entity.getId(), AprovacaoStatus.PENDENTE);
                for (AprovacaoEntity filha : filhasPendentes) {
                    enriquecerPayloadFilhaComDadosPaiSeNecessario(filha);
                    validarAusenciaConflitoRecursosAprovados(filha);
                }
            } else {
                if (entity.getAprovacaoPaiId() != null) {
                    enriquecerPayloadFilhaComDadosPaiSeNecessario(entity);
                }
                validarAusenciaConflitoRecursosAprovados(entity);
            }
        }

        if (request.status() == AprovacaoStatus.REPROVADA) {
            aprovacao.reprovar(aprovadorId, actorContext.role(), request.observacao(), Instant.now());
        } else {
            aprovacao.aprovar(aprovadorId, actorContext.role(), request.observacao(), Instant.now());
        }
        aprovacaoMapper.aplicar(aprovacao, entity);

        if (isMasterRecorrente) {
            List<AprovacaoEntity> filhasPendentes = aprovacaoJpaRepository.findByAprovacaoPaiIdAndStatus(
                    entity.getId(), AprovacaoStatus.PENDENTE);
            if (filhasPendentes.isEmpty()) {
                throw new ViolacaoRegraNegocioException(
                        "Todas as ocorrências filhas desta série recorrente já foram deliberadas individualmente.");
            }
            cascatearDecisaoFilhas(entity.getId(), request.status(), aprovadorId, actorContext, request.observacao());
            if (request.status() == AprovacaoStatus.REPROVADA) {
                return processRejection(aprovacao, entity, actorContext);
            }
            // Agregadora mestre consolida aprovação sem criar evento unitário próprio
            aprovacao.registrarExecucaoConcluida(Instant.now());
            aprovacaoMapper.aplicar(aprovacao, entity);
            aprovacaoJpaRepository.save(entity);
            publishDecisionEvent(entity, null);
            if (recalculoConflitoService != null) {
                recalculoConflitoService.recalcularConflitosPendentes(entity.getId());
            }
            return new DecisaoAprovacaoResponse(aprovacao.getId(), aprovacao.getStatus().name(),
                    new DecisaoAprovacaoResponse.ActionExecution("EXECUTED", null, null, null));
        }

        // Se for ocorrência filha pontual sendo decidida (já enriquecida acima quando APROVADA, antes da
        // checagem de conflito em linha 134; refazer aqui só é necessário no caminho de reprovação)
        if (entity.getAprovacaoPaiId() != null && request.status() != AprovacaoStatus.APROVADA) {
            enriquecerPayloadFilhaComDadosPaiSeNecessario(entity);
        }

        DecisaoAprovacaoResponse response;
        if (request.status() == AprovacaoStatus.REPROVADA) {
            response = processRejection(aprovacao, entity, actorContext);
        } else {
            response = processAprovacao(aprovacao, entity, actorContext);
        }

        // Se uma filha pontual foi decidida, verifica se todas as irmãs foram decididas para consolidar o pai
        if (entity.getAprovacaoPaiId() != null) {
            consolidarStatusPaiSeTodasFilhasDecididas(entity.getAprovacaoPaiId(), aprovadorId, actorContext);
        }

        return response;
    }

    private void enriquecerPayloadFilhaComDadosPaiSeNecessario(AprovacaoEntity filha) {
        AprovacaoEntity pai = aprovacaoJpaRepository.findById(filha.getAprovacaoPaiId()).orElse(null);
        if (pai == null || pai.getActionPayloadJson() == null) {
            return;
        }
        try {
            AprovacaoAcaoPayload payloadFilha = aprovacaoAcaoPayloadMapper.toPayload(filha.getActionPayloadJson());
            AprovacaoAcaoPayload payloadPai = aprovacaoAcaoPayloadMapper.toPayload(pai.getActionPayloadJson());
            if (payloadFilha != null && payloadPai != null) {
                UUID grupoId = payloadFilha.grupoParoquialResponsavelId() != null
                        ? payloadFilha.grupoParoquialResponsavelId()
                        : payloadPai.grupoParoquialResponsavelId();
                var categoria = payloadFilha.categoria() != null
                        ? payloadFilha.categoria()
                        : payloadPai.categoria();
                var status = payloadFilha.status() != null
                        ? payloadFilha.status()
                        : payloadPai.status();
                AprovacaoAcaoPayload enriched = new AprovacaoAcaoPayload(
                        payloadFilha.idempotencyKey(),
                        payloadFilha.eventoId(),
                        payloadFilha.titulo(),
                        payloadFilha.descricao(),
                        categoria,
                        grupoId,
                        payloadFilha.projetoId() != null ? payloadFilha.projetoId() : payloadPai.projetoId(),
                        payloadFilha.inicio(),
                        payloadFilha.fim(),
                        status,
                        payloadFilha.adicionadoExtraJustificativa(),
                        payloadFilha.canceladoMotivo(),
                        payloadFilha.motivo(),
                        payloadFilha.participantes(),
                        payloadFilha.localId() != null ? payloadFilha.localId() : payloadPai.localId(),
                        payloadFilha.igrejaVinculoId() != null
                                ? payloadFilha.igrejaVinculoId() : payloadPai.igrejaVinculoId(),
                        payloadFilha.ativoId(),
                        payloadFilha.ativoNome(),
                        payloadFilha.ativoIds(),
                        payloadFilha.justificativa()
                );
                filha.setActionPayloadJson(aprovacaoAcaoPayloadMapper.toJson(enriched));
            }
        } catch (RuntimeException ignored) {
        }
    }

    private void consolidarStatusPaiSeTodasFilhasDecididas(
            UUID paiId, String aprovadorId, EventoActorContext actorContext) {
        List<AprovacaoEntity> filhas = aprovacaoJpaRepository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(paiId);
        boolean hasPendentes = filhas.stream().anyMatch(f -> f.getStatus() == AprovacaoStatus.PENDENTE);
        if (hasPendentes) {
            return;
        }
        AprovacaoEntity pai = aprovacaoJpaRepository.findById(paiId).orElse(null);
        if (pai == null || pai.getStatus() != AprovacaoStatus.PENDENTE) {
            return;
        }
        boolean hasAprovada = filhas.stream().anyMatch(f -> f.getStatus() == AprovacaoStatus.APROVADA);
        Aprovacao paiDominio = aprovacaoMapper.toDomain(pai);
        if (hasAprovada) {
            paiDominio.aprovar(aprovadorId, actorContext.role(),
                    "Consolidação automática: série concluída com ocorrências autorizadas", Instant.now());
            paiDominio.registrarExecucaoConcluida(Instant.now());
        } else {
            paiDominio.reprovar(aprovadorId, actorContext.role(),
                    "Consolidação automática: todas as ocorrências da série foram reprovadas", Instant.now());
        }
        aprovacaoMapper.aplicar(paiDominio, pai);
        aprovacaoJpaRepository.save(pai);
        publishDecisionEvent(pai, null);
    }

    private void cascatearDecisaoFilhas(
            UUID masterId,
            AprovacaoStatus status,
            String aprovadorId,
            EventoActorContext actorContext,
            String observacao) {
        List<AprovacaoEntity> filhas = aprovacaoJpaRepository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(masterId);
        for (AprovacaoEntity filha : filhas) {
            if (filha.getStatus() == AprovacaoStatus.PENDENTE) {
                Aprovacao filhaDominio = aprovacaoMapper.toDomain(filha);
                if (status == AprovacaoStatus.REPROVADA) {
                    filhaDominio.reprovar(aprovadorId, actorContext.role(), observacao, Instant.now());
                    aprovacaoMapper.aplicar(filhaDominio, filha);
                    aprovacaoJpaRepository.save(filha);
                    publishDecisionEvent(filha, null);
                } else if (status == AprovacaoStatus.APROVADA) {
                    enriquecerPayloadFilhaComDadosPaiSeNecessario(filha);
                    filhaDominio.aprovar(aprovadorId, actorContext.role(), observacao, Instant.now());
                    TipoSolicitacaoInput tipoFilha = TipoSolicitacaoInput.fromJson(filha.getTipoSolicitacao());
                    EstrategiaExecucaoAprovacao stratFilha = strategies.stream()
                            .filter(s -> s.supports(tipoFilha))
                            .findFirst()
                            .orElse(null);
                    if (stratFilha != null) {
                        try {
                            stratFilha.execute(filha, actorContext);
                            filhaDominio.registrarExecucaoConcluida(Instant.now());
                        } catch (RuntimeException ex) {
                            boolean isNeg = isFalhaNegocio(ex);
                            TipoFalhaAprovacao tf = isNeg
                                    ? TipoFalhaAprovacao.NEGOCIO_CADASTRAL : TipoFalhaAprovacao.SISTEMICA_APLICACAO;
                            if (isNeg) {
                                LOGGER.warn("Ocorrência filha id={} falhou por regra cadastral/de negócio: {}",
                                        filha.getId(), ex.getMessage());
                            } else {
                                LOGGER.error("Falha sistêmica na ocorrência filha id={}, tipo={}",
                                        filha.getId(), tipoFilha, ex);
                            }
                            filhaDominio.registrarFalhaExecucao(tf, ex.getMessage(), extrairDetalheErro(ex));
                            publishDecisionEvent(filha, ex.getMessage());
                        }
                    }
                    aprovacaoMapper.aplicar(filhaDominio, filha);
                    aprovacaoJpaRepository.save(filha);
                    publishDecisionEvent(filha, null);
                }
            }
        }
    }

    private Instant inicioAcao(AprovacaoEntity entity) {
        if (entity.getAprovacaoPaiId() == null && aprovacaoJpaRepository.countByAprovacaoPaiId(entity.getId()) > 0) {
            return null;
        }
        try {
            AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(entity.getActionPayloadJson());
            return payload == null ? null : payload.inicio();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private DecisaoAprovacaoResponse processRejection(Aprovacao aprovacao, AprovacaoEntity entity,
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
            String tipoNome = aprovacao.getTipoSolicitacao() == null ? "UNKNOWN" : aprovacao.getTipoSolicitacao();
            metricsPublisher.publishAprovacaoFlow(tipoNome, "REJECTED");
            metricsPublisher.recordRejeicao(tipoNome);
            if (aprovacao.getCriadoEmUtc() != null && aprovacao.getDecididoEmUtc() != null) {
                metricsPublisher.publishAprovacaoDecisionLatency(
                        tipoNome,
                        AprovacaoStatus.REPROVADA.name(),
                        Duration.between(aprovacao.getCriadoEmUtc(), aprovacao.getDecididoEmUtc()));
            }
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

        return new DecisaoAprovacaoResponse(aprovacao.getId(), aprovacao.getStatus().name(),
                new DecisaoAprovacaoResponse.ActionExecution(
                        "REJECTED", aprovacao.getEventoId(), currentStatus, null));
    }

    private DecisaoAprovacaoResponse processAprovacao(Aprovacao aprovacao, AprovacaoEntity entity,
                                                      EventoActorContext actorContext) {
        TipoSolicitacaoInput tipo = TipoSolicitacaoInput.fromJson(aprovacao.getTipoSolicitacao());
        EstrategiaExecucaoAprovacao strategy = strategies.stream()
                .filter(s -> s.supports(tipo))
                .findFirst()
                .orElseThrow(() -> new AprovacaoExecucaoFalhouException(
                        "Unsupported aprovacao type: " + aprovacao.getTipoSolicitacao()));

        try {
            DecisaoAprovacaoResponse.ActionExecution execution = strategy.execute(entity, actorContext);
            finalizeSuccessfulExecution(aprovacao, entity, execution, actorContext, tipo);
            return new DecisaoAprovacaoResponse(aprovacao.getId(), aprovacao.getStatus().name(), execution);
        } catch (RuntimeException ex) {
            handleExecutionFailure(aprovacao, entity, ex, actorContext, tipo);
            throw new AprovacaoExecucaoFalhouException("Falha na execução: " + ex.getMessage());
        }
    }

    private void finalizeSuccessfulExecution(Aprovacao aprovacao, AprovacaoEntity entity,
                                             DecisaoAprovacaoResponse.ActionExecution execution,
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
            metricsPublisher.recordAprovacao(tipo.name());
            if (tipo == TipoSolicitacaoInput.CANCELAMENTO) {
                metricsPublisher.recordDelecao("EVENTO", "APROVACAO");
            } else if (tipo == TipoSolicitacaoInput.EDICAO_EVENTO || tipo == TipoSolicitacaoInput.ALTERACAO_HORARIO) {
                metricsPublisher.recordEdicao("EVENTO", "APROVACAO");
            }
            if (aprovacao.getCriadoEmUtc() != null && aprovacao.getDecididoEmUtc() != null) {
                metricsPublisher.publishAprovacaoDecisionLatency(
                        tipo.name(),
                        AprovacaoStatus.APROVADA.name(),
                        Duration.between(aprovacao.getCriadoEmUtc(), aprovacao.getDecididoEmUtc()));
            }
            if (aprovacao.getDecididoEmUtc() != null) {
                metricsPublisher.publishAprovacaoExecutionLatency(tipo.name(),
                        Duration.between(aprovacao.getDecididoEmUtc(), executedAt));
            }
            if (aprovacao.getCriadoEmUtc() != null) {
                metricsPublisher.publishAprovacaoTotalLatency(tipo.name(),
                        Duration.between(aprovacao.getCriadoEmUtc(), executedAt));
            }
        }
        publishDecisionEvent(entity, null);
        if (recalculoConflitoService != null) {
            recalculoConflitoService.recalcularConflitosPendentes(entity.getId());
        }
    }

    private void validarAusenciaConflitoRecursosAprovados(AprovacaoEntity entity) {
        if (detectorConflitoEventoService == null
                || entity.getActionPayloadJson() == null
                || entity.getActionPayloadJson().isBlank()) {
            return;
        }
        boolean temConflitoRecurso = detectarConflitosDeRecursos(entity).stream().anyMatch(c ->
                "LOCAL".equalsIgnoreCase(c.tipoConflito()) || "ATIVO".equalsIgnoreCase(c.tipoConflito()));
        if (temConflitoRecurso) {
            throw new ViolacaoRegraNegocioException(
                    "Não é permitido aprovar solicitação com conflito de espaço físico ou ativo "
                            + "patrimonial já reservado por outro evento aprovado.");
        }
    }

    /**
     * Isola a interpretação do payload de outro domínio (que pode falhar por formato inesperado) da regra de
     * negócio em si, evitando capturar-e-relançar {@link ViolacaoRegraNegocioException} sobre a checagem acima.
     */
    private List<ConflitoEventoItemDto> detectarConflitosDeRecursos(AprovacaoEntity entity) {
        try {
            AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(entity.getActionPayloadJson());
            if (payload == null || payload.inicio() == null || payload.fim() == null) {
                return List.of();
            }
            return detectorConflitoEventoService.detectarConflitos(
                    payload.inicio(),
                    payload.fim(),
                    payload.localId(),
                    payload.localIds(),
                    payload.ativoIds(),
                    payload.categoria(),
                    payload.eventoId(),
                    payload.tipoHorario(),
                    payload.igrejaVinculoId(),
                    payload.periodos()
            );
        } catch (RuntimeException ex) {
            // Se falhar a interpretação de payload de outro domínio, permite prosseguir
            return List.of();
        }
    }

    private void handleExecutionFailure(Aprovacao aprovacao, AprovacaoEntity entity, RuntimeException ex,
                                        EventoActorContext actorContext, TipoSolicitacaoInput tipo) {
        boolean isNegocio = isFalhaNegocio(ex);
        TipoFalhaAprovacao tipoFalha =
                isNegocio ? TipoFalhaAprovacao.NEGOCIO_CADASTRAL : TipoFalhaAprovacao.SISTEMICA_APLICACAO;
        String detalheErro = extrairDetalheErro(ex);

        if (isNegocio) {
            LOGGER.warn("Aprovação id={} falhou por regra cadastral/de negócio: {}",
                    entity.getId(), ex.getMessage());
        } else {
            LOGGER.error("Falha sistêmica ao executar aprovação id={}, tipoSolicitacao={}",
                    entity.getId(), tipo, ex);
        }

        aprovacao.registrarFalhaExecucao(tipoFalha, ex.getMessage(), detalheErro);
        aprovacaoMapper.aplicar(aprovacao, entity);
        aprovacaoJpaRepository.save(entity);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("error", ex.getClass().getSimpleName());
        metadata.put("tipoFalha", tipoFalha.name());
        metadata.put(KEY_DECISION_STATUS, AprovacaoStatus.APROVADA.name());
        if (actorContext.organizationId() != null) {
            metadata.put(KEY_GRUPO_PAROQUIAL_ID, actorContext.organizationId());
        }

        if (auditPublisher != null) {
            auditPublisher.publishAprovacaoDecision(actorContext.actor(), entity, "falhou", metadata);
        }
        if (metricsPublisher != null) {
            metricsPublisher.publishAprovacaoFlow(tipo.name(), "FAILED");
            metricsPublisher.recordFalhaExecucao(tipo.name(), tipoFalha.name());
        }
        publishDecisionEvent(entity, ex.getMessage());
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

    private void publishDecisionEvent(AprovacaoEntity entity, String errorMessage) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new AprovacaoDecisaoEvent(
                    entity.getId(), entity.getEventoId(), entity.getSolicitanteId(),
                    entity.getStatus(), entity.getAprovadorId(), errorMessage));
        }
    }
}
