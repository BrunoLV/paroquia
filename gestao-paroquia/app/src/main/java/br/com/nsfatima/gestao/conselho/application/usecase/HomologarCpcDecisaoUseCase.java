package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.domain.exception.CpcDecisaoNotFoundException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.conselho.api.v1.dto.CpcDecisaoResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.HomologarCpcDecisaoRequest;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.CpcDecisaoEntity;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository.CpcDecisaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class HomologarCpcDecisaoUseCase {

    private final CpcDecisaoJpaRepository repository;
    private final EventoActorContextResolver actorContextResolver;

    public HomologarCpcDecisaoUseCase(CpcDecisaoJpaRepository repository, EventoActorContextResolver actorContextResolver) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
    }

    @Transactional
    public CpcDecisaoResponse execute(UUID id, HomologarCpcDecisaoRequest request) {
        EventoActorContext context = actorContextResolver.resolveRequired();

        CpcDecisaoEntity entity = repository.findById(id)
                .orElseThrow(() -> new CpcDecisaoNotFoundException(id));

        if (request.status() == StatusHomologacao.VETADO && (request.motivoVeto() == null || request.motivoVeto().isBlank())) {
            throw new IllegalArgumentException("Motivo do veto é obrigatório ao vetar uma decisão");
        }

        entity.setStatusHomologacao(request.status());
        entity.setHomologadorId(context.actor());
        entity.setMotivoVeto(request.status() == StatusHomologacao.VETADO ? request.motivoVeto() : null);
        entity.setDecididoEmUtc(Instant.now());

        CpcDecisaoEntity saved = repository.save(entity);
        return CreateCpcDecisaoUseCase.toResponse(saved);
    }
}
