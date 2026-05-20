package br.com.nsfatima.gestao.projeto.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Indicadores de saúde temporal do projeto")
public record SaudeTemporalDTO(
        @Schema(description = "Percentual de tempo decorrido do projeto")
        double percentualTempoDecorrido,

        @Schema(description = "Indica se o projeto está em risco (prazo apertado com pendências)")
        boolean emRisco
) {
}
