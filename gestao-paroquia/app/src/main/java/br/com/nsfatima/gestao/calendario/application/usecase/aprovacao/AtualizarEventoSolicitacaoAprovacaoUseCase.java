package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoAprovacaoPendenteResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.AtualizarEventoRequest;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Atualiza o status de homologação ou solicitação de ajuste em evento pendente.
 */
@Service
public class AtualizarEventoSolicitacaoAprovacaoUseCase {

    private final AprovacaoJpaRepository aprovacaoRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final EventoAuditPublisher auditPublisher;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    public AtualizarEventoSolicitacaoAprovacaoUseCase(
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
     * Envia uma solicitação para alterar campos críticos de um evento existente, garantindo que mudanças em datas
     * ou dados essenciais sejam avaliadas pelo Conselho Paroquial.
     * <p>
     * Exemplo de uso:
     * {@code
     * useCase.execute(eventoId, updateRequest);
     * }
     */
    @Transactional
    public EventoAprovacaoPendenteResponse execute(UUID eventoId, AtualizarEventoRequest request) {
        EventoActorContext context = actorContextResolver.resolveRequired();

        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setEventoId(eventoId);
        entity.setTipoSolicitacao(TipoSolicitacaoInput.EDICAO_EVENTO.name());
        entity.setStatus(AprovacaoStatus.PENDENTE);
        entity.setSolicitanteId(context.actor());
        entity.setSolicitantePapel(context.role());
        entity.setSolicitanteTipoGrupo(TipoGrupo.fromStoredValue(context.organizationType()));
        entity.setAprovadorPapel(AprovadorPapel.COORD_CONSELHO.storedValue());
        entity.setCriadoEmUtc(Instant.now());

        AprovacaoAcaoPayload payload = buildPayload(eventoId, request);
        entity.setActionPayloadJson(aprovacaoAcaoPayloadMapper.toJson(payload));

        aprovacaoRepository.save(entity);

        auditPublisher.publish(context.actor(),
                "aprovacao-decisao-solicitacao", entity.getId().toString(), "sucesso", Map.of(

                        "tipoSolicitacao", entity.getTipoSolicitacao(),
                        "targetEventId", eventoId
                ));

        return new EventoAprovacaoPendenteResponse(
                entity.getId(),
                entity.getStatus().name(),
                "APROVACAO_PENDENTE");
    }

    private AprovacaoAcaoPayload buildPayload(UUID eventoId, AtualizarEventoRequest request) {
        return new AprovacaoAcaoPayload(
                null, // idempotencyKey
                eventoId,
                request.titulo(),
                request.descricao(),
                request.categoria(),
                request.grupoParoquialResponsavelId(),
                request.projetoId(),
                request.inicio(),
                request.fim(),
                null, // status
                request.adicionadoExtraJustificativa(),
                request.canceladoMotivo(),
                null, // motivo
                request.participantes(),
                request.localId(),
                request.igrejaVinculoId(),
                null, // ativoId
                null); // ativoNome
    }
}
