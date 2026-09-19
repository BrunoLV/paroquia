package br.com.nsfatima.gestao.calendario.api.v1.dto.metricas;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Delimita o intervalo de tempo e a granularidade usados no cálculo de uma métrica estratégica ou
 * preditiva, ecoado na resposta para que o consumidor da API saiba exatamente qual janela temporal
 * gerou o indicador retornado.
 */
@Schema(description = "Dados do período operacional analisado")
public record PeriodoOperacionalResponse(
        @Schema(description = "Granularidade do período", example = "mensal")
        String granularidade,

        @Schema(description = "Data de início (UTC)")
        Instant inicio,

        @Schema(description = "Data de término (UTC)")
        Instant fim) {
}
