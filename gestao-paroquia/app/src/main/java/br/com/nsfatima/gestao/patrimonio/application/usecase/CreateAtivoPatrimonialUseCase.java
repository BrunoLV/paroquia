package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.CreateAtivoPatrimonialRequest;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoGrupoParoquialPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.NumeroTombamentoGenerator;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.UUID;

/**
 * Cadastra um novo ativo patrimonial vinculado a uma organização proprietária, sempre no status
 * inicial {@code DISPONIVEL} e com um número de tombamento anual gerado automaticamente
 * ({@code NumeroTombamentoGenerator}).
 */
@Service
public class CreateAtivoPatrimonialUseCase {

    private final AtivoPatrimonialJpaRepository repository;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;
    private final AtivoGrupoParoquialPolicy grupoParoquialPolicy;
    private final NumeroTombamentoGenerator numeroTombamentoGenerator;
    private final PatrimonioAuditPublisher auditPublisher;

    public CreateAtivoPatrimonialUseCase(AtivoPatrimonialJpaRepository repository,
                                          EventoActorContextResolver actorContextResolver,
                                          PatrimonioAuthorizationService authorizationService,
                                          AtivoGrupoParoquialPolicy grupoParoquialPolicy,
                                          NumeroTombamentoGenerator numeroTombamentoGenerator,
                                          PatrimonioAuditPublisher auditPublisher) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.grupoParoquialPolicy = grupoParoquialPolicy;
        this.numeroTombamentoGenerator = numeroTombamentoGenerator;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Cadastra o ativo após validar que o ator pode criar ativos para a organização informada e
     * que essa organização é um tipo válido de proprietária de patrimônio.
     *
     * @param request dados do ativo a cadastrar
     * @return o ativo recém-criado, já com número de tombamento atribuído
     */
    @Transactional
    public AtivoPatrimonialResponse execute(CreateAtivoPatrimonialRequest request) {
        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanCreate(actorContext, request.grupoParoquialId());
        grupoParoquialPolicy.assertGrupoParoquialProprietariaPermitida(request.grupoParoquialId());

        AtivoPatrimonialEntity entity = new AtivoPatrimonialEntity();
        entity.setId(UUID.randomUUID());
        entity.setNome(request.nome());
        entity.setCategoria(request.categoria());
        entity.setStatus(AtivoStatus.DISPONIVEL);
        entity.setReservavel(request.reservavel());
        entity.setGrupoParoquialId(request.grupoParoquialId());
        entity.setDataAquisicao(request.dataAquisicao());
        entity.setValorEstimado(request.valorEstimado());
        entity.setCriadoEmUtc(Instant.now());
        entity.setNumeroTombamento(numeroTombamentoGenerator.generate(Year.now().getValue()));

        AtivoPatrimonialEntity saved = repository.save(entity);
        auditPublisher.publishCreateSuccess(actorContext.actor(), saved.getId(), saved.getGrupoParoquialId());
        return toResponse(saved);
    }

    /**
     * Converte a entidade JPA de ativo patrimonial para o DTO de resposta da API, reutilizado por
     * outros casos de uso do contexto que retornam o estado atualizado de um ativo.
     *
     * @param e entidade persistida
     * @return DTO de resposta correspondente
     */
    public static AtivoPatrimonialResponse toResponse(AtivoPatrimonialEntity e) {
        return new AtivoPatrimonialResponse(
                e.getId(),
                e.getNome(),
                e.getNumeroTombamento(),
                e.getCategoria(),
                e.getStatus(),
                e.isReservavel(),
                e.getGrupoParoquialId(),
                e.getDataAquisicao(),
                e.getValorEstimado(),
                e.getCriadoEmUtc());
    }
}
