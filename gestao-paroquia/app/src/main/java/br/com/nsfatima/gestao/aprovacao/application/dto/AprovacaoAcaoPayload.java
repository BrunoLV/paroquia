package br.com.nsfatima.gestao.aprovacao.application.dto;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.FormatoEvento;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Payload da ação de negócio pendente associada a uma solicitação de aprovação. Serializado em JSON (via
 * {@code AprovacaoAcaoPayloadMapper}) e armazenado em {@code AprovacaoEntity#actionPayloadJson} enquanto a
 * aprovação está PENDENTE, para que a ação (criação/atualização/cancelamento de evento, etc.) possa ser
 * efetivada posteriormente por uma {@code EstrategiaExecucaoAprovacao} sem depender do estado original da
 * requisição HTTP que a originou.
 */
public record AprovacaoAcaoPayload(
        String idempotencyKey,
        UUID eventoId,
        String titulo,
        String descricao,
        CategoriaEvento categoria,
        UUID grupoParoquialResponsavelId,
        UUID projetoId,
        Instant inicio,
        Instant fim,
        EventoStatusInput status,
        String adicionadoExtraJustificativa,
        String canceladoMotivo,
        String motivo,
        List<UUID> participantes,
        UUID localId,
        List<UUID> localIds,
        UUID igrejaVinculoId,
        UUID ativoId,
        String ativoNome,
        List<UUID> ativoIds,
        String justificativa,
        TipoHorarioEvento tipoHorario,
        FormatoEvento formato,
        List<TipoHorarioEvento> periodos) {

    public AprovacaoAcaoPayload {
        if (tipoHorario == null) {
            tipoHorario = TipoHorarioEvento.PERSONALIZADO;
        }
        if (localIds == null || localIds.isEmpty()) {
            localIds = localId != null ? List.of(localId) : List.of();
        } else if (localId == null) {
            localId = localIds.getFirst();
        }
        if (formato == null) {
            formato = !localIds.isEmpty() ? FormatoEvento.PRESENCIAL : FormatoEvento.ONLINE;
        }
        if (periodos == null) {
            periodos = List.of();
        }
    }

    public AprovacaoAcaoPayload(
            String idempotencyKey,
            UUID eventoId,
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID grupoParoquialResponsavelId,
            UUID projetoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            String canceladoMotivo,
            String motivo,
            List<UUID> participantes,
            UUID localId,
            List<UUID> localIds,
            UUID igrejaVinculoId,
            UUID ativoId,
            String ativoNome,
            List<UUID> ativoIds,
            String justificativa,
            TipoHorarioEvento tipoHorario,
            FormatoEvento formato) {
        this(idempotencyKey, eventoId, titulo, descricao, categoria, grupoParoquialResponsavelId, projetoId,
                inicio, fim, status, adicionadoExtraJustificativa, canceladoMotivo, motivo, participantes,
                localId, localIds, igrejaVinculoId, ativoId, ativoNome, ativoIds, justificativa, tipoHorario,
                formato, List.of());
    }

    public AprovacaoAcaoPayload(
            String idempotencyKey,
            UUID eventoId,
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID grupoParoquialResponsavelId,
            UUID projetoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            String canceladoMotivo,
            String motivo,
            List<UUID> participantes,
            UUID localId,
            UUID igrejaVinculoId,
            UUID ativoId,
            String ativoNome,
            List<UUID> ativoIds,
            String justificativa) {
        this(idempotencyKey, eventoId, titulo, descricao, categoria, grupoParoquialResponsavelId, projetoId,
                inicio, fim, status, adicionadoExtraJustificativa, canceladoMotivo, motivo, participantes,
                localId, localId != null ? List.of(localId) : List.of(), igrejaVinculoId, ativoId, ativoNome,
                ativoIds, justificativa, TipoHorarioEvento.PERSONALIZADO, null);
    }

    public AprovacaoAcaoPayload(
            String idempotencyKey,
            UUID eventoId,
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID grupoParoquialResponsavelId,
            UUID projetoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            String canceladoMotivo,
            String motivo,
            List<UUID> participantes,
            UUID localId,
            UUID igrejaVinculoId,
            UUID ativoId,
            String ativoNome) {
        this(idempotencyKey, eventoId, titulo, descricao, categoria, grupoParoquialResponsavelId, projetoId,
                inicio, fim, status, adicionadoExtraJustificativa, canceladoMotivo, motivo, participantes,
                localId, igrejaVinculoId, ativoId, ativoNome,
                ativoId != null ? List.of(ativoId) : List.of(),
                adicionadoExtraJustificativa);
    }

    public AprovacaoAcaoPayload(
            String idempotencyKey,
            UUID eventoId,
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID grupoParoquialResponsavelId,
            UUID projetoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            String canceladoMotivo,
            String motivo,
            List<UUID> participantes,
            UUID localId) {
        // Construtor de compatibilidade para chamadores que não lidam com alocação de ativo patrimonial.
        this(idempotencyKey,
                eventoId, titulo, descricao, categoria, grupoParoquialResponsavelId, projetoId, inicio, fim, status,
                adicionadoExtraJustificativa, canceladoMotivo, motivo, participantes, localId, null, null, null);
    }
}
