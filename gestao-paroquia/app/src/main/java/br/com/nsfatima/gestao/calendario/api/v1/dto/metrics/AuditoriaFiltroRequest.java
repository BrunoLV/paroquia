package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "Filtros para consulta da trilha de auditoria")
public record AuditoriaFiltroRequest(
        @Schema(description = "ID da organização", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID organizacaoId,

        @Schema(description = "Granularidade temporal (ex: diaria, semanal, mensal)")
        String granularidade,

        @Schema(description = "Data de início (ISO 8601)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant inicio,

        @Schema(description = "Data de término (ISO 8601)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant fim,

        @Schema(description = "Filtrar por ator")
        String ator,

        @Schema(description = "Filtrar por ação")
        String acao,

        @Schema(description = "Filtrar por resultado (ex: SUCCESS, FAILURE)")
        String resultado,

        @Schema(description = "Filtrar por ID de correlação")
        String correlationId) {
}
