package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoResponse;

import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoMetricsPublisher;
import br.com.nsfatima.gestao.aprovacao.domain.model.Aprovacao;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final AprovacaoMetricsPublisher metricsPublisher;

    public CriarSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoActorContextResolver eventoActorContextResolver,
            EventoAuditPublisher eventoAuditPublisher,
            AprovacaoMapper aprovacaoMapper) {
        this(aprovacaoJpaRepository, eventoActorContextResolver, eventoAuditPublisher, aprovacaoMapper, null);
    }

    @Autowired
    public CriarSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            EventoActorContextResolver eventoActorContextResolver,
            EventoAuditPublisher eventoAuditPublisher,
            AprovacaoMapper aprovacaoMapper,
            @Autowired(required = false) AprovacaoMetricsPublisher metricsPublisher) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
        this.eventoActorContextResolver = eventoActorContextResolver;
        this.eventoAuditPublisher = eventoAuditPublisher;
        this.aprovacaoMapper = aprovacaoMapper;
        this.metricsPublisher = metricsPublisher;
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
    public AprovacaoResponse criar(UUID eventoId, TipoSolicitacaoInput tipoSolicitacao) {
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

        if (metricsPublisher != null) {
            metricsPublisher.recordSolicitacaoAberta(tipoSolicitacao.name(), actorContext.role());
        }

        return aprovacaoMapper.toResponse(entity);
    }
}

