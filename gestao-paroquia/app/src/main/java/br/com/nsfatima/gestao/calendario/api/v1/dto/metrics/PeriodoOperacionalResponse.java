package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Dados do período operacional analisado")
public record PeriodoOperacionalResponse(
        @Schema(description = "Granularidade do período", example = "mensal")
        String granularidade,

        @Schema(description = "Data de início (UTC)")
        Instant inicio,

        @Schema(description = "Data de término (UTC)")
        Instant fim) {
}
