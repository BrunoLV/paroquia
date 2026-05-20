package br.com.nsfatima.gestao.projeto.application.usecase;

import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoCreateRequest;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAuthorizationService;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.observability.ProjetoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProjetoUseCase {

    private final ProjetoEventoJpaRepository repository;
    private final ProjetoMapper mapper;
    private final ProjetoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;
    private final ProjetoAuthorizationService authorizationService;

    /**
     * Inicializa o caso de uso com as dependências necessárias para persistência, auditoria e autorização.
     *
     * @param repository Repositório JPA para persistência de projetos
     * @param mapper Mapeador para conversão entre entidades e DTOs
     * @param auditPublisher Publicador de eventos de auditoria
     * @param actorContextResolver Resolvedor de contexto do ator da requisição
     * @param authorizationService Serviço de autorização para validar permissões
     */
    public CreateProjetoUseCase(
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
     * Coordena a criação de um novo projeto, garantindo que as regras de negócio sejam aplicadas e a ação seja auditada.
     *
     * <p>Usage Example:
     * {@code createProjetoUseCase.create(new ProjetoCreateRequest("Nome", "Desc", orgId, inicio, fim))}
     *
     * @param request Dados para criação do projeto
     * @return DTO com os dados do projeto criado
     * @throws IllegalArgumentException se a data de fim for anterior à de início
     * @throws SecurityException se o ator não tiver permissão para criar projetos na organização
     */
    @Transactional
    public ProjetoResponse create(ProjetoCreateRequest request) {
        var actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanCreate(actorContext, request.organizacaoResponsavelId());

        if (request.fim().isBefore(request.inicio())) {
            throw new IllegalArgumentException("Project end date cannot be before start date");
        }

        ProjetoEventoEntity entity = new ProjetoEventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setNome(request.nome());
        entity.setDescricao(request.descricao());
        entity.setOrganizacaoResponsavelId(request.organizacaoResponsavelId());
        entity.setInicioUtc(request.inicio());
        entity.setFimUtc(request.fim());
        
        ProjetoEventoEntity saved = repository.save(entity);
        
        auditPublisher.publishCreateSuccess(actorContext.actor(), saved.getId().toString());
        
        return mapper.toResponse(saved);
    }
}
