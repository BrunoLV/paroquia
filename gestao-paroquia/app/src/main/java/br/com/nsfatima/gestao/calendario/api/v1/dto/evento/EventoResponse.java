package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
        UUID igrejaVinculoId,
        List<UUID> participantes,
        UUID recorrenciaId,
        String estadoConflito,
        String motivoConflito) {
}
