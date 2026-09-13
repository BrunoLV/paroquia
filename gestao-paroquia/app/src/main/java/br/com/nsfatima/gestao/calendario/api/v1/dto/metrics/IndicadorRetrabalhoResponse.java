package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Resultado do cálculo do indicador de retrabalho de uma organização em um período: a proporção de
 * eventos que precisaram ser reagendados/refeitos em relação ao total de eventos afetados na
 * amostra analisada.
 *
 * @param numeradorOcorrenciasElegiveis quantidade de ocorrências que caracterizam retrabalho segundo
 *     a regra de negócio (ex: reagendamentos, cancelamentos seguidos de recriação)
 * @param denominadorEventosAfetados total de eventos considerados na amostra, usado como base do
 *     percentual em {@code taxaRetrabalho}
 */
@Schema(description = "Resposta contendo indicadores de retrabalho")
public record IndicadorRetrabalhoResponse(
        @Schema(description = "ID da organização analisada")
        UUID grupoParoquialId,

        @Schema(description = "Período de análise")
        PeriodoOperacionalResponse periodo,

        @Schema(description = "Percentual de retrabalho calculado", example = "5.2")
        BigDecimal taxaRetrabalho,

        @Schema(description = "Número de ocorrências que caracterizam retrabalho", example = "10")
        long numeradorOcorrenciasElegiveis,

        @Schema(description = "Número total de eventos afetados na amostra", example = "200")
        long denominadorEventosAfetados) {
}
