package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Resposta contendo indicadores de retrabalho")
public record IndicadorRetrabalhoResponse(
                @Schema(description = "ID da organização analisada")
                UUID organizacaoId,

                @Schema(description = "Período de análise")
                PeriodoOperacionalResponse periodo,

                @Schema(description = "Percentual de retrabalho calculado", example = "5.2")
                double taxaRetrabalho,

                @Schema(description = "Número de ocorrências que caracterizam retrabalho", example = "10")
                long numeradorOcorrenciasElegiveis,

                @Schema(description = "Número total de eventos afetados na amostra", example = "200")
                long denominadorEventosAfetados) {
}
