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

/**
 * Orquestra as transições de estado do ciclo de vida de um {@code Projeto}
 * ({@code ABERTO -> EM_ANDAMENTO -> CONCLUIDO}, com {@code CANCELADO} a partir de
 * {@code ABERTO} ou {@code EM_ANDAMENTO}).
 *
 * <p><b>Regra de desvio para aprovação no cancelamento:</b> ao contrário de
 * {@link #iniciar} e {@link #concluir}, que aplicam a transição diretamente quando o ator está
 * autorizado, {@link #cancelar} verifica {@code ProjetoAuthorizationService#isHighLevelActor}.
 * Se o ator for de alto nível (ex.: PAROCO, ADMIN, coordenador de conselho pastoral), o
 * cancelamento é aplicado imediatamente e os eventos vinculados são cancelados ou desvinculados
 * conforme {@code cancelarEventosVinculados}. Caso contrário — mesmo que o ator tenha permissão
 * para cancelar segundo {@code ProjetoAuthorizationService#assertCanCancel} — a mudança de
 * status NÃO é aplicada de imediato: é criada uma {@code ProjetoSolicitacaoAprovacaoEntity}
 * pendente (tipo {@code CANCELAMENTO}), e o projeto permanece no status atual até que um ator
 * de alto nível decida a solicitação via {@link #decidir}. Esse desvio evita que atores
 * intermediários encerrem projetos e seus eventos vinculados sem revisão de uma autoridade
 * superior.
 *
 * <p>{@link #decidir} também processa solicitações de criação de projeto (tipo
 * {@code CRIACAO}, originadas em {@code CreateProjetoUseCase}) reconstruindo o payload de
 * criação persistido em JSON quando aprovadas.
 */
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

    /**
     * Transiciona o projeto de {@code ABERTO} para {@code EM_ANDAMENTO}. Aplicada diretamente
     * (sem passar por aprovação) mediante autorização de
     * {@code ProjetoAuthorizationService#assertCanStartOrConclude}.
     *
     * @param id ID do projeto a iniciar.
     * @return o projeto atualizado com status {@code EM_ANDAMENTO}.
     * @throws ProjetoNotFoundException se o projeto não existir.
     * @throws IllegalStateException    se o projeto não estiver no status {@code ABERTO}.
     */
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

    /**
     * Transiciona o projeto de {@code EM_ANDAMENTO} para {@code CONCLUIDO}. Aplicada
     * diretamente (sem passar por aprovação) mediante autorização de
     * {@code ProjetoAuthorizationService#assertCanStartOrConclude}.
     *
     * @param id ID do projeto a concluir.
     * @return o projeto atualizado com status {@code CONCLUIDO}.
     * @throws ProjetoNotFoundException se o projeto não existir.
     * @throws IllegalStateException    se o projeto não estiver no status {@code EM_ANDAMENTO}.
     */
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

    /**
     * Cancela o projeto (a partir de {@code ABERTO} ou {@code EM_ANDAMENTO}), ou solicita seu
     * cancelamento para aprovação, dependendo do nível do ator.
     *
     * <p>Se o ator for de alto nível ({@code ProjetoAuthorizationService#isHighLevelActor}), o
     * cancelamento é aplicado imediatamente: o projeto vai para {@code CANCELADO} e os eventos
     * vinculados são cancelados (se {@code cancelarEventosVinculados} for {@code true}) ou
     * desvinculados do projeto (caso contrário). Caso contrário, nenhuma mudança de status é
     * aplicada: é criada uma {@code ProjetoSolicitacaoAprovacaoEntity} pendente do tipo
     * {@code CANCELAMENTO}, a ser decidida posteriormente por um ator de alto nível via
     * {@link #decidir}.
     *
     * @param id                          ID do projeto a cancelar.
     * @param cancelarEventosVinculados   se {@code true}, eventos vinculados são marcados como
     *                                    cancelados; se {@code false}, apenas são desvinculados
     *                                    do projeto. Só tem efeito imediato para atores de alto
     *                                    nível — para os demais, a decisão fica registrada na
     *                                    solicitação de aprovação.
     * @return resultado contendo o {@code ProjetoResponse} atualizado (cancelamento imediato) ou
     *         o {@code ProjetoAprovacaoResponse} da solicitação pendente (desvio para
     *         aprovação) — nunca ambos preenchidos.
     * @throws ProjetoNotFoundException se o projeto não existir.
     * @throws IllegalStateException    se o projeto não estiver em {@code ABERTO} ou
     *                                   {@code EM_ANDAMENTO}.
     */
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

    /**
     * Decide (aprova ou reprova) uma solicitação de aprovação pendente relacionada a um
     * projeto — de criação ({@code CRIACAO}) ou de cancelamento ({@code CANCELAMENTO}). Só pode
     * ser executada por um ator de alto nível
     * ({@code ProjetoAuthorizationService#isHighLevelActor}), reforçando que o desvio de
     * aprovação criado em {@link #cancelar} (ou na criação de projeto) só pode ser resolvido por
     * uma autoridade superior.
     *
     * <p>Quando aprovada: para {@code CRIACAO}, reconstrói o {@code ProjetoCreateRequest} a
     * partir do payload JSON persistido na solicitação e efetivamente cria o projeto (status
     * {@code ABERTO}); para {@code CANCELAMENTO}, aplica o cancelamento no projeto referenciado
     * e cancela/desvincula os eventos vinculados, conforme
     * {@code cancelarEventosVinculados} registrado na solicitação original. Quando reprovada,
     * apenas o status da solicitação é atualizado — nenhuma mudança é aplicada ao projeto ou aos
     * seus eventos.
     *
     * @param solicitacaoId ID da solicitação de aprovação a decidir.
     * @param aprovado      {@code true} para aprovar, {@code false} para reprovar.
     * @param observacao    observação opcional registrada junto à decisão.
     * @return a solicitação de aprovação atualizada, já com o resultado da decisão.
     * @throws IllegalArgumentException    se a solicitação não existir.
     * @throws IllegalStateException       se a solicitação já tiver sido decidida anteriormente.
     * @throws ForbiddenOperationException se o ator não for de alto nível.
     */
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
