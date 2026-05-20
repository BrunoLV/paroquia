package br.com.nsfatima.gestao.governanca.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Resumo do estado de um ano paroquial")
public record AnoParoquialResponse(
        @Schema(description = "O ano civil")
        Integer ano,
        
        @Schema(description = "Status do ano (PLANEJAMENTO, FECHADO)")
        String status,
        
        @Schema(description = "Data em que o ano foi fechado")
        Instant dataFechamentoUtc,
        
        @Schema(description = "Última atualização")
        Instant updatedAt) {
}
