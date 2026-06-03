package br.com.nsfatima.gestao.projeto.application.usecase;

import br.com.nsfatima.gestao.projeto.application.dto.ProjetoCreationResult;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoAprovacaoResponse;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoCreateRequest;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.domain.exception.ProjetoNotFoundException;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAuthorizationService;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoSolicitacaoAprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;

import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransitionProjetoUseCase {

    private final ProjetoEventoJpaRepository repository;
    private final ProjetoSolicitacaoAprovacaoJpaRepository approvalRepository;
    private final EventoJpaRepository eventoRepository;
    private final ProjetoMapper mapper;
    private final ProjetoAuthorizationService authorizationService;
    private final EventoActorContextResolver actorContextResolver;
    private final ObjectMapper objectMapper;

    public TransitionProjetoUseCase(
            ProjetoEventoJpaRepository repository,
            ProjetoSolicitacaoAprovacaoJpaRepository approvalRepository,
            EventoJpaRepository eventoRepository,
            ProjetoMapper mapper,
            ProjetoAuthorizationService authorizationService,
            EventoActorContextResolver actorContextResolver,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.approvalRepository = approvalRepository;
        this.eventoRepository = eventoRepository;
        this.mapper = mapper;
        this.authorizationService = authorizationService;
        this.actorContextResolver = actorContextResolver;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProjetoResponse iniciar(UUID id) {
        ProjetoEventoEntity project = repository.findById(id)
                .orElseThrow(() -> new ProjetoNotFoundException(id));

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanStartOrConclude(actorContext, project.getOrganizacaoResponsavelId());

        if (project.getStatusEnum() != ProjetoStatus.ABERTO) {
            throw new IllegalStateException("Only ABERTO projects can be started");
        }

        project.setStatus(ProjetoStatus.EM_ANDAMENTO);
        ProjetoEventoEntity saved = repository.save(project);
        return mapper.toResponse(saved);
    }

    @Transactional
    public ProjetoResponse concluir(UUID id) {
        ProjetoEventoEntity project = repository.findById(id)
                .orElseThrow(() -> new ProjetoNotFoundException(id));

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanStartOrConclude(actorContext, project.getOrganizacaoResponsavelId());

        if (project.getStatusEnum() != ProjetoStatus.EM_ANDAMENTO) {
            throw new IllegalStateException("Only EM_ANDAMENTO projects can be completed");
        }

        project.setStatus(ProjetoStatus.CONCLUIDO);
        ProjetoEventoEntity saved = repository.save(project);
        return mapper.toResponse(saved);
    }

    @Transactional
    public ProjetoCreationResult cancelar(UUID id, boolean cancelarEventosVinculados) {
        ProjetoEventoEntity project = repository.findById(id)
                .orElseThrow(() -> new ProjetoNotFoundException(id));

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanCancel(actorContext, project.getOrganizacaoResponsavelId());

        if (project.getStatusEnum() != ProjetoStatus.ABERTO && project.getStatusEnum() != ProjetoStatus.EM_ANDAMENTO) {
            throw new IllegalStateException("Only ABERTO or EM_ANDAMENTO projects can be cancelled");
        }

        if (authorizationService.isHighLevelActor(actorContext)) {
            // Cancel immediately
            project.setStatus(ProjetoStatus.CANCELADO);
            ProjetoEventoEntity savedProject = repository.save(project);

            // Handle linked events
            List<EventoEntity> linkedEvents = eventoRepository.findByProjetoId(id);
            if (cancelarEventosVinculados) {
                for (EventoEntity event : linkedEvents) {
                    event.setStatus("CANCELADO");
                }
            } else {
                for (EventoEntity event : linkedEvents) {
                    event.setProjetoId(null);
                }
            }
            eventoRepository.saveAll(linkedEvents);

            return new ProjetoCreationResult(mapper.toResponse(savedProject), null);
        } else {
            // Create a pending cancellation request
            ProjetoSolicitacaoAprovacaoEntity approval = new ProjetoSolicitacaoAprovacaoEntity();
            approval.setId(UUID.randomUUID());
            approval.setProjetoId(id);
            approval.setTipoSolicitacao("CANCELAMENTO");
            approval.setStatus("PENDENTE");
            approval.setSolicitanteId(actorContext.actor());
            approval.setSolicitantePapel(actorContext.role());
            approval.setSolicitanteTipoOrganizacao(actorContext.organizationType());
            approval.setCancelarEventosVinculados(cancelarEventosVinculados);
            approval.setCriadoEmUtc(Instant.now());

            ProjetoSolicitacaoAprovacaoEntity savedApproval = approvalRepository.save(approval);
            return new ProjetoCreationResult(null, mapper.toAprovacaoResponse(savedApproval));
        }
    }

    @Transactional
    public ProjetoAprovacaoResponse decidir(UUID solicitacaoId, boolean aprovado, String observacao) {
        ProjetoSolicitacaoAprovacaoEntity req = approvalRepository.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found: " + solicitacaoId));

        if (!"PENDENTE".equals(req.getStatus())) {
            throw new IllegalStateException("This request is already decided");
        }

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        if (!authorizationService.isHighLevelActor(actorContext)) {
            throw new ForbiddenOperationException("Only ADMIN, PAROCO or CONSELHO coordinators can decide on project requests");
        }

        req.setStatus(aprovado ? "APROVADA" : "REJEITADA");
        req.setDecididoEmUtc(Instant.now());
        req.setAprovadorId(actorContext.actor());
        req.setDecisionObservacao(observacao);

        if ("CRIACAO".equals(req.getTipoSolicitacao())) {
            if (aprovado) {
                try {
                    ProjetoCreateRequest creationPayload = objectMapper.readValue(
                            req.getActionPayloadJson(), ProjetoCreateRequest.class);

                    ProjetoEventoEntity project = new ProjetoEventoEntity();
                    project.setId(UUID.randomUUID());
                    project.setNome(creationPayload.nome());
                    project.setDescricao(creationPayload.descricao());
                    project.setOrganizacaoResponsavelId(creationPayload.organizacaoResponsavelId());
                    project.setInicioUtc(creationPayload.inicio());
                    project.setFimUtc(creationPayload.fim());
                    project.setStatus(ProjetoStatus.ABERTO);

                    ProjetoEventoEntity saved = repository.save(project);
                    req.setProjetoId(saved.getId());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to process approved project creation", e);
                }
            }
        } else if ("CANCELAMENTO".equals(req.getTipoSolicitacao())) {
            if (aprovado) {
                ProjetoEventoEntity project = repository.findById(req.getProjetoId())
                        .orElseThrow(() -> new ProjetoNotFoundException(req.getProjetoId()));

                project.setStatus(ProjetoStatus.CANCELADO);
                repository.save(project);

                List<EventoEntity> linkedEvents = eventoRepository.findByProjetoId(project.getId());
                if (Boolean.TRUE.equals(req.getCancelarEventosVinculados())) {
                    for (EventoEntity event : linkedEvents) {
                        event.setStatus("CANCELADO");
                    }
                } else {
                    for (EventoEntity event : linkedEvents) {
                        event.setProjetoId(null);
                    }
                }
                eventoRepository.saveAll(linkedEvents);
            }
        }

        ProjetoSolicitacaoAprovacaoEntity savedRequest = approvalRepository.save(req);
        return mapper.toAprovacaoResponse(savedRequest);
    }
}
