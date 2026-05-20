package br.com.nsfatima.gestao.calendario.api.v1.dto.observacao;

import java.time.Instant;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoResponse;

public record ObservacaoResponse(
                UUID id,
                UUID eventoId,
                UUID usuarioId,
                TipoObservacaoResponse tipo,
                String conteudo,
                Instant criadoEmUtc) {
}
