package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Resposta contendo a trilha de auditoria de operações")
public record AuditoriaOperacaoResponse(
        @Schema(description = "ID da organização analisada")
        UUID organizacaoId,

        @Schema(description = "Período da trilha")
        PeriodoOperacionalResponse periodo,

        @Schema(description = "Itens da trilha de auditoria")
        List<RegistroAuditavelItem> items) {

    @Schema(description = "Item individual da trilha de auditoria")
    public record RegistroAuditavelItem(
            @Schema(description = "ID único do registro")
            UUID id,

            @Schema(description = "ID da organização onde a ação ocorreu")
            UUID organizacaoId,

            @Schema(description = "Tipo de recurso afetado", example = "evento")
            String recursoTipo,

            @Schema(description = "ID do recurso afetado")
            String recursoId,

            @Schema(description = "Ação realizada", example = "create-event")
            String acao,

            @Schema(description = "Resultado da ação", example = "SUCCESS")
            String resultado,

            @Schema(description = "Identificação do ator que realizou a ação")
            String ator,

            @Schema(description = "ID de correlação da operação")
            String correlationId,

            @Schema(description = "Data e hora da ocorrência (UTC)")
            Instant ocorridoEmUtc,

            @Schema(description = "Detalhes técnicos adicionais da operação")
            Map<String, Object> detalhesAuditaveis) {
    }
}
