package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta contendo a taxa de eventos adicionados extra-ordinariamente")
public record TaxaEventosExtraResponse(
        @Schema(description = "Período de análise", example = "anual")
        String periodo,

        @Schema(description = "Percentual de eventos adicionados extra-ordinariamente", example = "15.5")
        double taxaEventosAdicionadosExtra) {
}
