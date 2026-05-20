package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import java.time.Instant;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusResponse;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;

public record EventoResponse(
        UUID id,
        String titulo,
        String descricao,
        CategoriaEvento categoria,
        UUID organizacaoResponsavelId,
        UUID projetoId,
        String nomeProjeto,
        Instant inicio,
        Instant fim,
        EventoStatusResponse status,
        String conflictState,
        String conflictReason) {
}
