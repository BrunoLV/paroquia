package br.com.nsfatima.gestao.governanca.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Representa, para a API, o estado de governança de um ano paroquial: em que fase do ciclo de vida
 * ({@code EM_PLANEJAMENTO}, {@code FECHADO}, {@code CONCLUIDO}) ele se encontra e quando a trava foi aplicada.
 */
@Schema(description = "Resumo do estado de um ano paroquial")
public record AnoParoquialResponse(
        @Schema(description = "O ano civil")
        Integer ano,

        @Schema(description = "Status do ano (EM_PLANEJAMENTO, FECHADO, CONCLUIDO)")
        String status,

        @Schema(description = "Data em que o ano foi fechado")
        Instant dataFechamentoUtc,

        @Schema(description = "Última atualização")
        Instant updatedAt) {
}
