package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import java.time.Instant;
import java.util.List;
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
        UUID localId,
        List<UUID> participantes,
        UUID recorrenciaId,
        String conflictState,
        String conflictReason) {
}
