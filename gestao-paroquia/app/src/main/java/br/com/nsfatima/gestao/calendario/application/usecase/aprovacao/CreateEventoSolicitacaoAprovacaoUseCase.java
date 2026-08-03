package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CreateEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoAprovacaoPendenteResponse;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class CreateEventoSolicitacaoAprovacaoUseCase {

    private final AprovacaoJpaRepository aprovacaoRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final EventoAuditPublisher auditPublisher;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    public CreateEventoSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoRepository,
            EventoActorContextResolver actorContextResolver,
            EventoAuditPublisher auditPublisher,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper) {
        this.aprovacaoRepository = aprovacaoRepository;
        this.actorContextResolver = actorContextResolver;
        this.auditPublisher = auditPublisher;
        this.aprovacaoAcaoPayloadMapper = aprovacaoAcaoPayloadMapper;
    }

    /**
     * Records a formal request to create a new event that requires higher-level authorization due to its sensitivity
     * or the proposer's role.
     * <p>
     * Usage Example:
     * {@code
     * useCase.execute("idempotency-key", createRequest);
     * }
     */
    @Transactional
    public EventoAprovacaoPendenteResponse execute(String idempotencyKey, CreateEventoRequest request) {
        EventoActorContext context = actorContextResolver.resolveRequired();

        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
        entity.setStatus(AprovacaoStatus.PENDENTE);
        entity.setSolicitanteId(context.actor());
        entity.setSolicitantePapel(context.role());
        entity.setSolicitanteTipoOrganizacao(TipoOrganizacao.fromStoredValue(context.organizationType()));
        entity.setAprovadorPapel(AprovadorPapel.COORD_CONSELHO.storedValue());
        entity.setCriadoEmUtc(Instant.now());

        AprovacaoAcaoPayload payload = buildPayload(idempotencyKey, request);
        entity.setActionPayloadJson(aprovacaoAcaoPayloadMapper.toJson(payload));

        aprovacaoRepository.save(entity);

        auditPublisher.publish(context.actor(), "aprovacao-decisao-solicitacao", entity.getId().toString(), "sucesso");

        return new EventoAprovacaoPendenteResponse(
                entity.getId(),
                entity.getStatus().name(),
                "APROVACAO_PENDENTE");
    }

    private AprovacaoAcaoPayload buildPayload(String idempotencyKey, CreateEventoRequest request) {
        return new AprovacaoAcaoPayload(
                idempotencyKey,
                null, // eventoId
                request.titulo(),
                request.descricao(),
                request.categoria(),
                request.organizacaoResponsavelId(),
                request.projetoId(),
                request.inicio(),
                request.fim(),
                request.status(),
                request.adicionadoExtraJustificativa(),
                null, // canceladoMotivo
                null, // motivo
                request.participantes(),
                request.localId(),
                request.igrejaVinculoId(),
                null, // ativoId
                null); // ativoNome
    }
}
