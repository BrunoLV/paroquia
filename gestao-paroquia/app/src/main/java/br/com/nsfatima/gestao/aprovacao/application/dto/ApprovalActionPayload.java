package br.com.nsfatima.gestao.aprovacao.application.dto;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ApprovalActionPayload(
        String idempotencyKey,
        UUID eventoId,
        String titulo,
        String descricao,
        CategoriaEvento categoria,
        UUID organizacaoResponsavelId,
        UUID projetoId,
        Instant inicio,
        Instant fim,
        EventoStatusInput status,
        String adicionadoExtraJustificativa,
        String canceladoMotivo,
        String motivo,
        List<UUID> participantes,
        UUID localId,
        UUID igrejaVinculoId) {

    public ApprovalActionPayload(
            String idempotencyKey,
            UUID eventoId,
            String titulo,
            String descricao,
            CategoriaEvento categoria,
            UUID organizacaoResponsavelId,
            UUID projetoId,
            Instant inicio,
            Instant fim,
            EventoStatusInput status,
            String adicionadoExtraJustificativa,
            String canceladoMotivo,
            String motivo,
            List<UUID> participantes,
            UUID localId) {
        this(idempotencyKey,
                eventoId, titulo, descricao, categoria, organizacaoResponsavelId, projetoId, inicio, fim, status,
                adicionadoExtraJustificativa, canceladoMotivo, motivo, participantes, localId, null);

    }
}
