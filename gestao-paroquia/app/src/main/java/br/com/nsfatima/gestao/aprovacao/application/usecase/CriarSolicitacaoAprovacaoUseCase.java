package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.domain.model.Aprovacao;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Caso de uso para abertura de solicitações formais de aprovação de eventos pastorais.
 *
 * REGRA DE NEGÓCIO:
 * Cria a solicitação em estado PENDENTE para posterior chancela pela autoridade eclesiástica competente.
 */
@Service
public class CriarSolicitacaoAprovacaoUseCase {

    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final EventoActorContextResolver eventoActorContextResolver;
    private final EventoAuditPublisher eventoAuditPublisher;
    private final AprovacaoMapper aprovacaoMapper;

    public CriarSolicitacaoAprovacaoUseCase(
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
     * Inicia o fluxo de governança, persistindo uma solicitação pendente e notificando o sistema de auditoria.
     * <p>
     * Exemplo de uso:
     * {@code
     * useCase.create(eventoId, TipoSolicitacaoInput.CREATE);
     * }
     */
    @Transactional
    public AprovacaoResponse create(UUID eventoId, TipoSolicitacaoInput tipoSolicitacao) {
        EventoActorContext actorContext = eventoActorContextResolver.resolveRequired();
        AprovadorPapel aprovadorPapel = AprovadorPapel.resolveForAprovacao(actorContext.role(),
                actorContext.organizationType());


        Aprovacao aprovacao = Aprovacao.solicitar(
                UUID.randomUUID(),
                eventoId,
                tipoSolicitacao.name(),
                aprovadorPapel.storedValue(),
                actorContext.usuarioId().toString(),
                null,
                Instant.now());

        AprovacaoEntity entity = aprovacaoMapper.toEntity(aprovacao);
        entity.setSolicitantePapel(actorContext.role());
        entity.setSolicitanteTipoGrupo(TipoGrupo.fromStoredValue(actorContext.organizationType()));

        aprovacaoJpaRepository.save(entity);

        if (eventoAuditPublisher != null) {
            eventoAuditPublisher.publishCreatePendente(
                    actorContext.actor(),
                    entity.getId().toString(),
                    "N/A");
        }

        return aprovacaoMapper.toResponse(entity);
    }
}
