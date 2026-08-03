package br.com.nsfatima.gestao.projeto.application.usecase;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoAprovacaoResponse;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoCreateRequest;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.application.dto.ProjetoCreationResult;
import br.com.nsfatima.gestao.projeto.domain.exception.ProjetoNotFoundException;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAuthorizationService;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoSolicitacaoAprovacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TransitionProjetoUseCase {

    private final ProjetoEventoJpaRepository repository;
    private final ProjetoSolicitacaoAprovacaoJpaRepository aprovacaoRepository;
    private final EventoJpaRepository eventoRepository;
    private final ProjetoMapper mapper;
    private final ProjetoAuthorizationService authorizationService;
    private final EventoActorContextResolver actorContextResolver;
    private final ObjectMapper objectMapper;

    public TransitionProjetoUseCase(
            ProjetoEventoJpaRepository repository,
            ProjetoSolicitacaoAprovacaoJpaRepository aprovacaoRepository,
            EventoJpaRepository eventoRepository,
            ProjetoMapper mapper,
            ProjetoAuthorizationService authorizationService,
            EventoActorContextResolver actorContextResolver,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.aprovacaoRepository = aprovacaoRepository;
        this.eventoRepository = eventoRepository;
        this.mapper = mapper;
        this.authorizationService = authorizationService;
        this.actorContextResolver = actorContextResolver;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProjetoResponse iniciar(UUID id) {
        ProjetoEventoEntity projeto = repository.findById(id)
                .orElseThrow(() -> new ProjetoNotFoundException(id));

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanStartOrConclude(actorContext, projeto.getOrganizacaoResponsavelId());

        if (projeto.getStatus() != ProjetoStatus.ABERTO) {
            throw new IllegalStateException("Somente projetos ABERTO podem ser iniciados");
        }

        projeto.setStatus(ProjetoStatus.EM_ANDAMENTO);
        ProjetoEventoEntity saved = repository.save(projeto);
        return mapper.toResponse(saved);
    }

    @Transactional
    public ProjetoResponse concluir(UUID id) {
        ProjetoEventoEntity projeto = repository.findById(id)
                .orElseThrow(() -> new ProjetoNotFoundException(id));

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanStartOrConclude(actorContext, projeto.getOrganizacaoResponsavelId());

        if (projeto.getStatus() != ProjetoStatus.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente projetos EM_ANDAMENTO podem ser concluídos");
        }

        projeto.setStatus(ProjetoStatus.CONCLUIDO);
        ProjetoEventoEntity saved = repository.save(projeto);
        return mapper.toResponse(saved);
    }

    @Transactional
    public ProjetoCreationResult cancelar(UUID id, boolean cancelarEventosVinculados) {
        ProjetoEventoEntity projeto = repository.findById(id)
                .orElseThrow(() -> new ProjetoNotFoundException(id));

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanCancel(actorContext, projeto.getOrganizacaoResponsavelId());

        if (projeto.getStatus() != ProjetoStatus.ABERTO && projeto.getStatus() != ProjetoStatus.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente projetos ABERTO ou EM_ANDAMENTO podem ser cancelados");
        }

        if (authorizationService.isHighLevelActor(actorContext)) {
            // Cancelar imediatamente
            projeto.setStatus(ProjetoStatus.CANCELADO);
            ProjetoEventoEntity projetoSalvo = repository.save(projeto);

            // Tratar eventos vinculados
            List<EventoEntity> eventosVinculados = eventoRepository.findByProjetoId(id);
            if (cancelarEventosVinculados) {
                for (EventoEntity evento : eventosVinculados) {
                    evento.setStatus(EventoStatusInput.CANCELADO.name());
                }
            } else {
                for (EventoEntity evento : eventosVinculados) {
                    evento.setProjetoId(null);
                }
            }
            eventoRepository.saveAll(eventosVinculados);

            return new ProjetoCreationResult(mapper.toResponse(projetoSalvo), null);
        } else {
            // Criar uma solicitação de cancelamento pendente
            ProjetoSolicitacaoAprovacaoEntity aprovacao = new ProjetoSolicitacaoAprovacaoEntity();
            aprovacao.setId(UUID.randomUUID());
            aprovacao.setProjetoId(id);
            aprovacao.setTipoSolicitacao("CANCELAMENTO");
            aprovacao.setStatus(AprovacaoStatus.PENDENTE);
            aprovacao.setSolicitanteId(actorContext.actor());
            aprovacao.setSolicitantePapel(actorContext.role());
            aprovacao.setSolicitanteTipoOrganizacao(TipoOrganizacao.fromStoredValue(actorContext.organizationType()));
            aprovacao.setCancelarEventosVinculados(cancelarEventosVinculados);
            aprovacao.setCriadoEmUtc(Instant.now());

            ProjetoSolicitacaoAprovacaoEntity savedAprovacao = aprovacaoRepository.save(aprovacao);
            return new ProjetoCreationResult(null, mapper.toAprovacaoResponse(savedAprovacao));
        }
    }

    @Transactional
    public ProjetoAprovacaoResponse decidir(UUID solicitacaoId, boolean aprovado, String observacao) {
        ProjetoSolicitacaoAprovacaoEntity req = aprovacaoRepository.findById(solicitacaoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Solicitação de aprovação não encontrada: " + solicitacaoId));

        if (req.getStatus() != AprovacaoStatus.PENDENTE) {
            throw new IllegalStateException("Esta solicitação já foi decidida");
        }

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        if (!authorizationService.isHighLevelActor(actorContext)) {
            throw new ForbiddenOperationException(
                    "Apenas ADMIN, PAROCO ou coordenadores do CONSELHO podem decidir solicitações de projeto");
        }

        req.setStatus(aprovado ? AprovacaoStatus.APROVADA : AprovacaoStatus.REPROVADA);
        req.setDecididoEmUtc(Instant.now());
        req.setAprovadorId(actorContext.actor());
        req.setDecisionObservacao(observacao);

        if ("CRIACAO".equals(req.getTipoSolicitacao())) {
            if (aprovado) {
                try {
                    ProjetoCreateRequest payloadCriacao = objectMapper.readValue(
                            req.getActionPayloadJson(), ProjetoCreateRequest.class);

                    ProjetoEventoEntity projeto = new ProjetoEventoEntity();
                    projeto.setId(UUID.randomUUID());
                    projeto.setNome(payloadCriacao.nome());
                    projeto.setDescricao(payloadCriacao.descricao());
                    projeto.setOrganizacaoResponsavelId(payloadCriacao.organizacaoResponsavelId());
                    projeto.setInicioUtc(payloadCriacao.inicio());
                    projeto.setFimUtc(payloadCriacao.fim());
                    projeto.setStatus(ProjetoStatus.ABERTO);

                    ProjetoEventoEntity saved = repository.save(projeto);
                    req.setProjetoId(saved.getId());
                } catch (Exception e) {
                    throw new RuntimeException("Falha ao processar a criação de projeto aprovada", e);
                }
            }
        } else if ("CANCELAMENTO".equals(req.getTipoSolicitacao())) {
            if (aprovado) {
                ProjetoEventoEntity projeto = repository.findById(req.getProjetoId())
                        .orElseThrow(() -> new ProjetoNotFoundException(req.getProjetoId()));

                projeto.setStatus(ProjetoStatus.CANCELADO);
                repository.save(projeto);

                List<EventoEntity> eventosVinculados = eventoRepository.findByProjetoId(projeto.getId());
                if (Boolean.TRUE.equals(req.getCancelarEventosVinculados())) {
                    for (EventoEntity evento : eventosVinculados) {
                        evento.setStatus(EventoStatusInput.CANCELADO.name());
                    }
                } else {
                    for (EventoEntity evento : eventosVinculados) {
                        evento.setProjetoId(null);
                    }
                }
                eventoRepository.saveAll(eventosVinculados);
            }
        }

        ProjetoSolicitacaoAprovacaoEntity solicitacaoSalva = aprovacaoRepository.save(req);
        return mapper.toAprovacaoResponse(solicitacaoSalva);
    }
}
