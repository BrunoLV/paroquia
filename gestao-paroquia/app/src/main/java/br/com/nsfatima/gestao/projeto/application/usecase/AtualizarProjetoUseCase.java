package br.com.nsfatima.gestao.projeto.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoPatchRequest;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.domain.exception.ProjetoNotFoundException;
import br.com.nsfatima.gestao.projeto.domain.model.Projeto;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.application.service.ProjetoAuthorizationService;
import br.com.nsfatima.gestao.projeto.infrastructure.observability.ProjetoAuditPublisher;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Atualiza os dados de planejamento, metas ou escopo de um Projeto pastoral existente.
 */
@Service
public class AtualizarProjetoUseCase {

    private final ProjetoEventoJpaRepository repository;
    private final ProjetoMapper mapper;
    private final ProjetoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;
    private final ProjetoAuthorizationService authorizationService;

    /**
     * Inicializa o caso de uso com as dependências para atualização, auditoria e controle de acesso.
     *
     * @param repository           Repositório JPA para busca e persistência de projetos
     * @param mapper               Mapeador para conversão entre entidades e DTOs
     * @param auditPublisher       Publicador de eventos de auditoria
     * @param actorContextResolver Resolvedor de contexto do ator da requisição
     * @param authorizationService Serviço de autorização para validar permissões de edição
     */
    public AtualizarProjetoUseCase(
            ProjetoEventoJpaRepository repository,
            ProjetoMapper mapper,
            ProjetoAuditPublisher auditPublisher,
            EventoActorContextResolver actorContextResolver,
            ProjetoAuthorizationService authorizationService) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
    }

    /**
     * Executa a atualização parcial de um projeto, validando permissões e mantendo a integridade das datas e status.
     *
     * <p>Usage Example:
     * {@code updateProjetoUseCase.execute(projetoId, new ProjetoPatchRequest("Novo Nome", null, null, null, null,
     * null))}
     *
     * @param id      ID único do projeto a ser atualizado
     * @param request Dados para atualização (campos nulos são ignorados)
     * @return DTO com os dados do projeto atualizado
     * @throws ProjetoNotFoundException se o ID não corresponder a um projeto existente
     * @throws SecurityException        se o ator não tiver permissão para editar o projeto
     * @throws IllegalArgumentException se a nova data de fim for anterior à de início
     */
    @Transactional
    public ProjetoResponse execute(UUID id, ProjetoPatchRequest request) {
        ProjetoEventoEntity entity = repository.findById(id)
                .orElseThrow(() -> new ProjetoNotFoundException(id));

        var actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanEdit(actorContext, entity.getGrupoParoquialResponsavelId());

        Projeto projeto = mapper.toDomain(entity);

        if (request.nome() != null) {
            projeto.renomear(request.nome());
        }
        if (request.descricao() != null) {
            projeto.alterarDescricao(request.descricao());
        }
        if (request.grupoParoquialResponsavelId() != null) {
            projeto.transferirResponsavel(request.grupoParoquialResponsavelId());
        }
        if (request.inicio() != null || request.fim() != null) {
            Instant novoInicio = request.inicio() != null ? request.inicio() : projeto.getInicio();
            Instant novoFim = request.fim() != null ? request.fim() : projeto.getFim();
            projeto.redefinirPeriodo(novoInicio, novoFim);
        }
        aplicarTransicaoStatus(projeto, request.status());

        mapper.aplicar(projeto, entity);
        ProjetoEventoEntity saved = repository.save(entity);

        auditPublisher.publishPatchSuccess(actorContext.actor(), saved.getId().toString());

        return mapper.toResponse(saved, true);
    }

    private void aplicarTransicaoStatus(Projeto projeto, ProjetoStatus alvo) {
        if (alvo == null || alvo == projeto.getStatus()) {
            return;
        }
        switch (alvo) {
            case EM_ANDAMENTO -> projeto.iniciar();
            case CONCLUIDO -> projeto.concluir();
            case ABERTO, CANCELADO -> throw new IllegalArgumentException(
                    "Transição para " + alvo + " via PATCH não é suportada; use os endpoints de transição do projeto.");
            default -> throw new IllegalArgumentException("Status de projeto não suportado: " + alvo);
        }
    }
}
