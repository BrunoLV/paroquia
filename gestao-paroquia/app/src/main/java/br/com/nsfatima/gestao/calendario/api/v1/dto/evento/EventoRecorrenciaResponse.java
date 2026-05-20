package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import java.util.UUID;

public record EventoRecorrenciaResponse(
        UUID id,
        UUID eventoBaseId,
        String frequencia,
        int intervalo) {
}
