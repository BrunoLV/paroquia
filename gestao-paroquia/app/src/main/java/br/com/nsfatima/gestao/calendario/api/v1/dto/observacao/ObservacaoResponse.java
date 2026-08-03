package br.com.nsfatima.gestao.calendario.api.v1.dto.observacao;

import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoResponse;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa uma anotação/observação registrada sobre um evento (ex: comentário operacional,
 * alerta), preservando quem a criou e quando, para consulta no histórico do evento.
 */
public record ObservacaoResponse(
        UUID id,
        UUID eventoId,
        UUID usuarioId,
        TipoObservacaoResponse tipo,
        String conteudo,
        Instant criadoEmUtc) {
}
