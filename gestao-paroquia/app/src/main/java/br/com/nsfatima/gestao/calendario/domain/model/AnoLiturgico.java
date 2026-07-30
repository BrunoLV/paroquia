package br.com.nsfatima.gestao.calendario.domain.model;

import java.time.Instant;

public record AnoLiturgico(
        Integer ano,
        String cicloLetra,
        Instant dataInicioAdvento,
        Instant dataFimComum,
        Instant atualizadoEmUtc
) {
}
