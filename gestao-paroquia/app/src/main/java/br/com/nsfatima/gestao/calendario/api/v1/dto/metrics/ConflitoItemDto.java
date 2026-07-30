package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

import java.util.UUID;

/**
 * Detalhe individual de sobreposição de eventos (Overlap Conflict).
 */
public record ConflitoItemDto(
        UUID eventoIdA,
        String tituloEventoA,
        String pastoralA,
        UUID eventoIdB,
        String tituloEventoB,
        String pastoralB,
        String dataEvento,
        String publicoAlvoCompartilhado,
        String mensagemAlerta
) {
}
