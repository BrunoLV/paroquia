package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import java.time.Instant;
import java.util.UUID;

public record EventoCanceladoResponse(
        UUID id,
        String status,
        String canceladoMotivo,
        String titulo,
        Instant inicio,
        Instant fim,
        UUID organizacaoResponsavelId) {
}
