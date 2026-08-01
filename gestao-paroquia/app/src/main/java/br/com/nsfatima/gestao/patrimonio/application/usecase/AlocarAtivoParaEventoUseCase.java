package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.aprovacao.application.dto.ApprovalActionPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AlocacaoAtivoApprovalPendingResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AlocarAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoOperationResult;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.application.usecase.observacao.CreateNotaAtivoPatrimonialUseCase;
import br.com.nsfatima.gestao.patrimonio.domain.event.AlocacaoAtivoPendenteEvent;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoVinculacaoPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AlocarAtivoParaEventoUseCase {

    private static final String TIPO_NOTA_ALOCACAO = "ALOCACAO";

    private final AtivoPatrimonialJpaRepository ativoRepository;
    private final EventoJpaRepository eventoRepository;
    private final LocalJpaRepository localRepository;
    private final AtivoAlocacaoJpaRepository alocacaoRepository;
    private final AprovacaoJpaRepository aprovacaoRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;
    private final AtivoVinculacaoPolicy vinculacaoPolicy;
    private final CreateNotaAtivoPatrimonialUseCase createNotaUseCase;
    private final PatrimonioAuditPublisher auditPublisher;
    private final ApprovalActionPayloadMapper payloadMapper;
    private final ApplicationEventPublisher eventPublisher;

    public AlocarAtivoParaEventoUseCase(AtivoPatrimonialJpaRepository ativoRepository,
                                        EventoJpaRepository eventoRepository,
                                        LocalJpaRepository localRepository,
                                        AtivoAlocacaoJpaRepository alocacaoRepository,
                                        AprovacaoJpaRepository aprovacaoRepository,
                                        EventoActorContextResolver actorContextResolver,
                                        PatrimonioAuthorizationService authorizationService,
                                        AtivoVinculacaoPolicy vinculacaoPolicy,
                                        CreateNotaAtivoPatrimonialUseCase createNotaUseCase,
                                        PatrimonioAuditPublisher auditPublisher,
                                        ApprovalActionPayloadMapper payloadMapper,
                                        ApplicationEventPublisher eventPublisher) {
        this.ativoRepository = ativoRepository;
        this.eventoRepository = eventoRepository;
        this.localRepository = localRepository;
        this.alocacaoRepository = alocacaoRepository;
        this.aprovacaoRepository = aprovacaoRepository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.vinculacaoPolicy = vinculacaoPolicy;
        this.createNotaUseCase = createNotaUseCase;
        this.auditPublisher = auditPublisher;
        this.payloadMapper = payloadMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AtivoOperationResult execute(UUID ativoId, AlocarAtivoRequest request) {
        AtivoPatrimonialEntity ativo = findAtivo(ativoId);

        vinculacaoPolicy.assertCanAlocar(ativo, request.dataInicio(), request.dataFim(),
                request.justificativaAlocacao());

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanAlocar(actorContext, ativo.getOrganizacaoId());

        EventoEntity evento = null;
        LocalEntity local = null;
        if (request.eventoId() != null) {
            EventoEntity foundEvento = eventoRepository.findById(request.eventoId())
                    .orElseThrow(() -> new EventoNotFoundException(request.eventoId()));
            local = localRepository.findById(foundEvento.getLocalId())
                    .orElseThrow(() -> new AtivoAlocacaoInvalidaException(
                            "Local não encontrado: " + foundEvento.getLocalId()));
            evento = foundEvento;
        }

        boolean isEmprestimo = local != null && !ativo.getOrganizacaoId().equals(local.getOrganizacaoId());

        if (!isEmprestimo) {
            AtivoAlocacaoEntity ocupacao = criarOcupacao(ativo, evento, local, request, AtivoAlocacaoStatus.CONFIRMADA);
            alocacaoRepository.save(ocupacao);
            AtivoPatrimonialResponse response = applyAlocacao(ativo, ocupacao, actorContext.actor(),
                    actorContext.usuarioId());
            return new AtivoOperationResult.Success(response, HttpStatus.OK);
        }

        AlocacaoAtivoApprovalPendingResponse pending = createPendingApproval(ativo, evento, local, request,
                actorContext);
        return new AtivoOperationResult.Pending(pending, HttpStatus.ACCEPTED);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AtivoPatrimonialResponse executeApprovedAlocacao(UUID aprovacaoId, String actor, UUID usuarioId) {
        AtivoAlocacaoEntity ocupacao = alocacaoRepository.findByAprovacaoId(aprovacaoId)
                .orElseThrow(() -> new AtivoAlocacaoInvalidaException(
                        "Ocupação não encontrada para a aprovação: " + aprovacaoId));

        AtivoPatrimonialEntity ativo = findAtivo(ocupacao.getAtivoId());

        vinculacaoPolicy.assertCanReconfirmar(ativo, ocupacao.getId(), ocupacao.getDataInicio(),
                ocupacao.getDataFim());

        ocupacao.setStatus(AtivoAlocacaoStatus.CONFIRMADA);
        alocacaoRepository.save(ocupacao);

        return applyAlocacao(ativo, ocupacao, actor, usuarioId);
    }

    private AtivoPatrimonialEntity findAtivo(UUID ativoId) {
        return ativoRepository.findById(ativoId)
                .orElseThrow(() -> new AtivoPatrimonialNotFoundException(
                        "Ativo patrimonial não encontrado: " + ativoId));
    }

    private AtivoPatrimonialResponse applyAlocacao(AtivoPatrimonialEntity ativo, AtivoAlocacaoEntity ocupacao,
                                                    String actor, UUID usuarioId) {
        ativo.setStatus(AtivoStatus.ALOCADO);
        AtivoPatrimonialEntity saved = ativoRepository.save(ativo);

        createNotaUseCase.execute(saved.getId(), usuarioId, TIPO_NOTA_ALOCACAO, ocupacao.getJustificativa());
        auditPublisher.publishAlocacao(actor, saved.getId(), saved.getOrganizacaoId(), ocupacao.getEventoId());

        return CreateAtivoPatrimonialUseCase.toResponse(saved);
    }

    private AtivoAlocacaoEntity criarOcupacao(AtivoPatrimonialEntity ativo, EventoEntity evento, LocalEntity local,
                                              AlocarAtivoRequest request, AtivoAlocacaoStatus status) {
        AtivoAlocacaoEntity ocupacao = new AtivoAlocacaoEntity();
        ocupacao.setId(UUID.randomUUID());
        ocupacao.setAtivoId(ativo.getId());
        ocupacao.setTipo(AtivoAlocacaoTipo.ALOCACAO_EVENTO);
        ocupacao.setEventoId(evento != null ? evento.getId() : null);
        ocupacao.setOrganizacaoOrigemId(ativo.getOrganizacaoId());
        ocupacao.setOrganizacaoDestinoId(local != null ? local.getOrganizacaoId() : null);
        ocupacao.setEmprestimo(local != null && !ativo.getOrganizacaoId().equals(local.getOrganizacaoId()));
        ocupacao.setDataInicio(request.dataInicio());
        ocupacao.setDataFim(request.dataFim());
        ocupacao.setStatus(status);
        ocupacao.setJustificativa(request.justificativaAlocacao());
        ocupacao.setCriadoEmUtc(Instant.now());
        return ocupacao;
    }

    private AlocacaoAtivoApprovalPendingResponse createPendingApproval(AtivoPatrimonialEntity ativo,
                                                                        EventoEntity evento, LocalEntity local,
                                                                        AlocarAtivoRequest request,
                                                                        EventoActorContext actorContext) {
        AtivoAlocacaoEntity ocupacao = criarOcupacao(ativo, evento, local, request,
                AtivoAlocacaoStatus.PENDENTE_APROVACAO);
        ocupacao.setSolicitanteId(actorContext.usuarioId());
        alocacaoRepository.save(ocupacao);

        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(UUID.randomUUID());
        aprovacao.setEventoId(evento.getId());
        aprovacao.setTipoSolicitacao(TipoSolicitacaoInput.ALOCACAO_ATIVO_EMPRESTIMO.name());
        aprovacao.setAprovadorPapel(AprovadorPapel.COORD_CONSELHO);
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setSolicitanteId(actorContext.usuarioId() != null ? actorContext.usuarioId().toString() : null);
        aprovacao.setSolicitantePapel(actorContext.role());
        aprovacao.setSolicitanteTipoOrganizacao(actorContext.organizationType());
        aprovacao.setActionPayloadJson(payloadMapper.toJson(new ApprovalActionPayload(
                null, evento.getId(), null, null, null, ativo.getOrganizacaoId(), null, null, null, null,
                null, null, request.justificativaAlocacao(), null, local.getId(), null, ativo.getId(),
                ativo.getNome())));
        aprovacao.setCorrelationId(ocupacao.getId().toString());
        aprovacaoRepository.save(aprovacao);

        ocupacao.setAprovacaoId(aprovacao.getId());
        alocacaoRepository.save(ocupacao);

        eventPublisher.publishEvent(new AlocacaoAtivoPendenteEvent(
                aprovacao.getId(), ativo.getId(), evento.getId(), aprovacao.getSolicitanteId()));

        return new AlocacaoAtivoApprovalPendingResponse(aprovacao.getId(), aprovacao.getStatusEnum().name(),
                ativo.getId(), evento.getId(), "APPROVAL_PENDING");
    }
}
