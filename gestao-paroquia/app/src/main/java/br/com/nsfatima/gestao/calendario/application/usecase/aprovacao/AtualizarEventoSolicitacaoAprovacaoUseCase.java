package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoAprovacaoPendenteResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.AtualizarEventoRequest;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.FormatoEvento;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
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
    private final EventoJpaRepository eventoJpaRepository;

    public AtualizarEventoSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoRepository,
            EventoActorContextResolver actorContextResolver,
            EventoAuditPublisher auditPublisher,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper) {
        this(aprovacaoRepository, actorContextResolver, auditPublisher, aprovacaoAcaoPayloadMapper, null);
    }

    @Autowired
    public AtualizarEventoSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoRepository,
            EventoActorContextResolver actorContextResolver,
            EventoAuditPublisher auditPublisher,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper,
            @Autowired(required = false) EventoJpaRepository eventoJpaRepository) {
        this.aprovacaoRepository = aprovacaoRepository;
        this.actorContextResolver = actorContextResolver;
        this.auditPublisher = auditPublisher;
        this.aprovacaoAcaoPayloadMapper = aprovacaoAcaoPayloadMapper;
        this.eventoJpaRepository = eventoJpaRepository;
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

        EventoEntity eventoExistente = eventoJpaRepository != null && eventoId != null
                ? eventoJpaRepository.findById(eventoId).orElse(null)
                : null;

        Instant dataOcorrencia = request.inicio();
        if (dataOcorrencia == null && eventoExistente != null) {
            dataOcorrencia = eventoExistente.getInicioUtc();
        }

        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setEventoId(eventoId);
        entity.setTipoSolicitacao(TipoSolicitacaoInput.EDICAO_EVENTO.name());
        entity.setStatus(AprovacaoStatus.PENDENTE);
        entity.setSolicitanteId(context.actor());
        entity.setSolicitantePapel(context.role());
        entity.setSolicitanteTipoOrganizacao(TipoOrganizacao.fromStoredValue(context.organizationType()));
        entity.setAprovadorPapel(AprovadorPapel.PAROCO.storedValue());
        entity.setCriadoEmUtc(Instant.now());
        entity.setDataOcorrenciaUtc(dataOcorrencia);

        AprovacaoAcaoPayload payload = buildPayload(eventoId, request, eventoExistente);
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

    private AprovacaoAcaoPayload buildPayload(
            UUID eventoId, AtualizarEventoRequest request, EventoEntity eventoExistente) {
        String titulo = request.titulo() != null
                ? request.titulo() : (eventoExistente != null ? eventoExistente.getTitulo() : null);
        String descricao = request.descricao() != null
                ? request.descricao() : (eventoExistente != null ? eventoExistente.getDescricao() : null);
        CategoriaEvento categoria = request.categoria() != null
                ? request.categoria() : (eventoExistente != null ? eventoExistente.getCategoria() : null);
        UUID grupoId = request.organizacaoResponsavelId() != null
                ? request.organizacaoResponsavelId()
                : (eventoExistente != null ? eventoExistente.getOrganizacaoResponsavelId() : null);
        UUID projetoId = request.projetoId() != null
                ? request.projetoId() : (eventoExistente != null ? eventoExistente.getProjetoId() : null);
        Instant inicio = request.inicio() != null
                ? request.inicio() : (eventoExistente != null ? eventoExistente.getInicioUtc() : null);
        Instant fim = request.fim() != null
                ? request.fim() : (eventoExistente != null ? eventoExistente.getFimUtc() : null);
        TipoHorarioEvento tipoHorario = request.tipoHorario() != null
                ? request.tipoHorario() : (eventoExistente != null ? eventoExistente.getTipoHorario() : null);
        FormatoEvento formato = request.formato() != null
                ? request.formato() : (eventoExistente != null ? eventoExistente.getFormato() : null);
        List<TipoHorarioEvento> periodos = request.periodos() != null && !request.periodos().isEmpty()
                ? request.periodos()
                : (eventoExistente != null ? eventoExistente.getPeriodosList() : List.of());

        List<UUID> localIds = request.localIds() != null && !request.localIds().isEmpty()
                ? request.localIds()
                : (eventoExistente != null
                        ? eventoExistente.getLocalIds()
                        : (request.localId() != null ? List.of(request.localId()) : List.of()));
        UUID localId = request.localId() != null
                ? request.localId()
                : (!localIds.isEmpty()
                        ? localIds.getFirst()
                        : (eventoExistente != null ? eventoExistente.getLocalId() : null));

        UUID igrejaVinculoId = request.igrejaVinculoId() != null
                ? request.igrejaVinculoId()
                : (eventoExistente != null ? eventoExistente.getIgrejaVinculoId() : null);
        List<UUID> ativoIds = request.ativoIds() != null ? request.ativoIds() : List.of();

        return new AprovacaoAcaoPayload(
                null, // idempotencyKey
                eventoId,
                titulo,
                descricao,
                categoria,
                grupoId,
                projetoId,
                inicio,
                fim,
                null, // status
                request.adicionadoExtraJustificativa(),
                request.canceladoMotivo(),
                null, // motivo
                request.participantes(),
                localId,
                localIds,
                igrejaVinculoId,
                null, // ativoId
                null, // ativoNome
                ativoIds,
                request.adicionadoExtraJustificativa(),
                tipoHorario,
                formato,
                periodos);
    }
}
