package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoResponse;
import br.com.nsfatima.gestao.calendario.domain.policy.AuthorizationPolicy;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateSolicitacaoAprovacaoUseCase {

    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final EventoActorContextResolver eventoActorContextResolver;
    private final EventoAuditPublisher eventoAuditPublisher;
    private final AprovacaoMapper aprovacaoMapper;

    @Autowired
    public CreateSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoActorContextResolver eventoActorContextResolver,
            EventoAuditPublisher eventoAuditPublisher,
            AprovacaoMapper aprovacaoMapper) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.eventoAuditPublisher = eventoAuditPublisher;
        this.aprovacaoMapper = aprovacaoMapper;
    }

    /**
     * Initiates the governance workflow by persisting a pending request and notifying the auditing system.
     * 
     * Usage Example:
     * {@code
     * useCase.create(eventoId, TipoSolicitacaoInput.CREATE);
     * }
     */
    @Transactional
    public AprovacaoResponse create(UUID eventoId, TipoSolicitacaoInput tipoSolicitacao) {
        EventoActorContext actorContext = eventoActorContextResolver.resolveRequired();
        AprovadorPapel aprovadorPapel = AprovadorPapel.resolveForApproval(actorContext.role(), actorContext.organizationType());

        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setEventoId(eventoId);
        entity.setTipoSolicitacao(tipoSolicitacao.name());
        entity.setAprovadorPapel(aprovadorPapel);
        entity.setStatus(AprovacaoStatus.PENDENTE);
        entity.setCriadoEmUtc(Instant.now());
        entity.setDecididoEmUtc(null);

        entity.setSolicitanteId(actorContext.usuarioId().toString());
        entity.setSolicitantePapel(actorContext.role());
        entity.setSolicitanteTipoOrganizacao(actorContext.organizationType());

        aprovacaoJpaRepository.save(entity);

        if (eventoAuditPublisher != null) {
            eventoAuditPublisher.publishCreatePending(
                    actorContext.actor(),
                    entity.getId().toString(),
                    "N/A");
        }

        return aprovacaoMapper.toResponse(entity);
    }
}
