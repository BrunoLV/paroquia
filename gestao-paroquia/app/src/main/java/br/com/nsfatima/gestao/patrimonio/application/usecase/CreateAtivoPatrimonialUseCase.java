package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.CreateAtivoPatrimonialRequest;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoOrganizacaoPolicy;
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

@Service
public class CreateAtivoPatrimonialUseCase {

    private final AtivoPatrimonialJpaRepository repository;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;
    private final AtivoOrganizacaoPolicy organizacaoPolicy;
    private final NumeroTombamentoGenerator numeroTombamentoGenerator;
    private final PatrimonioAuditPublisher auditPublisher;

    public CreateAtivoPatrimonialUseCase(AtivoPatrimonialJpaRepository repository,
                                          EventoActorContextResolver actorContextResolver,
                                          PatrimonioAuthorizationService authorizationService,
                                          AtivoOrganizacaoPolicy organizacaoPolicy,
                                          NumeroTombamentoGenerator numeroTombamentoGenerator,
                                          PatrimonioAuditPublisher auditPublisher) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.organizacaoPolicy = organizacaoPolicy;
        this.numeroTombamentoGenerator = numeroTombamentoGenerator;
        this.auditPublisher = auditPublisher;
    }

    @Transactional
    public AtivoPatrimonialResponse execute(CreateAtivoPatrimonialRequest request) {
        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanCreate(actorContext, request.organizacaoId());
        organizacaoPolicy.assertOwnerOrganizacaoPermitida(request.organizacaoId());

        AtivoPatrimonialEntity entity = new AtivoPatrimonialEntity();
        entity.setId(UUID.randomUUID());
        entity.setNome(request.nome());
        entity.setCategoria(request.categoria());
        entity.setStatus(AtivoStatus.DISPONIVEL);
        entity.setReservavel(request.reservavel());
        entity.setOrganizacaoId(request.organizacaoId());
        entity.setDataAquisicao(request.dataAquisicao());
        entity.setValorEstimado(request.valorEstimado());
        entity.setCriadoEmUtc(Instant.now());
        entity.setNumeroTombamento(numeroTombamentoGenerator.generate(Year.now().getValue()));

        AtivoPatrimonialEntity saved = repository.save(entity);
        auditPublisher.publishCreateSuccess(actorContext.actor(), saved.getId(), saved.getOrganizacaoId());
        return toResponse(saved);
    }

    public static AtivoPatrimonialResponse toResponse(AtivoPatrimonialEntity e) {
        return new AtivoPatrimonialResponse(
                e.getId(),
                e.getNome(),
                e.getNumeroTombamento(),
                e.getCategoria(),
                e.getStatus(),
                e.isReservavel(),
                e.getOrganizacaoId(),
                e.getDataAquisicao(),
                e.getValorEstimado(),
                e.getCriadoEmUtc());
    }
}
