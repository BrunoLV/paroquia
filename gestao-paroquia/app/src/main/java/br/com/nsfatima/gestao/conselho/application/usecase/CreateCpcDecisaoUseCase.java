package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.conselho.api.v1.dto.CreateCpcDecisaoRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.CpcDecisaoResponse;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.CpcDecisaoEntity;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository.CpcDecisaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateCpcDecisaoUseCase {

    private final CpcDecisaoJpaRepository repository;
    private final EventoActorContextResolver actorContextResolver;

    public CreateCpcDecisaoUseCase(CpcDecisaoJpaRepository repository, EventoActorContextResolver actorContextResolver) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
    }

    @Transactional
    public CpcDecisaoResponse execute(CreateCpcDecisaoRequest request) {
        EventoActorContext context = actorContextResolver.resolveRequired();

        CpcDecisaoEntity entity = new CpcDecisaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo(request.titulo());
        entity.setPautaDescricao(request.pautaDescricao());
        entity.setDataReuniao(request.dataReuniao());
        entity.setComunidadeId(request.comunidadeId());
        entity.setConselhoTipo("CPC");
        entity.setStatusHomologacao(StatusHomologacao.PENDENTE_HOMOLOGACAO);
        entity.setSolicitanteId(context.actor());
        entity.setSolicitanteNome(context.role());
        entity.setCriadoEmUtc(Instant.now());

        CpcDecisaoEntity saved = repository.save(entity);
        return toResponse(saved);
    }

    public static CpcDecisaoResponse toResponse(CpcDecisaoEntity e) {
        return new CpcDecisaoResponse(
                e.getId(),
                e.getTitulo(),
                e.getPautaDescricao(),
                e.getDataReuniao(),
                e.getComunidadeId(),
                e.getConselhoTipo(),
                e.getStatusHomologacao(),
                e.getSolicitanteId(),
                e.getSolicitanteNome(),
                e.getHomologadorId(),
                e.getMotivoVeto(),
                e.getCriadoEmUtc(),
                e.getDecididoEmUtc()
        );
    }
}
