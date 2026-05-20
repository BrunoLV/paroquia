package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
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
        List<UUID> participantes) {
}
