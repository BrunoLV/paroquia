package br.com.nsfatima.gestao.projeto.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Indicadores de saúde temporal do projeto, usados para sinalizar risco de atraso a partir do
 * percentual de tempo já decorrido em relação ao período planejado do projeto.
 */
@Schema(description = "Indicadores de saúde temporal do projeto")
public record SaudeTemporalDTO(
        @Schema(description = "Percentual de tempo decorrido do projeto")
        BigDecimal percentualTempoDecorrido,

        @Schema(description = "Indica se o projeto está em risco (prazo apertado com pendências)")
        boolean emRisco
) {
}
