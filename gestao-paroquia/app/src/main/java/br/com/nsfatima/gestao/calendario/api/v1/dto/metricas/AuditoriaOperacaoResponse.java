package br.com.nsfatima.gestao.calendario.api.v1.dto.metricas;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Trilha de auditoria de operações sobre o calendário para uma organização e período, consultada
 * pelos painéis de governança para investigar quem realizou o quê, quando e com qual resultado.
 */
@Schema(description = "Resposta contendo a trilha de auditoria de operações")
public record AuditoriaOperacaoResponse(
        @Schema(description = "ID da organização analisada")
        UUID grupoParoquialId,

        @Schema(description = "Período da trilha")
        PeriodoOperacionalResponse periodo,

        @Schema(description = "Itens da trilha de auditoria")
        List<RegistroAuditavelItem> items) {

    /**
     * Um único registro auditável, capturando a ação realizada, o ator responsável e o resultado
     * (sucesso ou falha), com detalhes técnicos adicionais em formato livre.
     *
     * @param detalhesAuditaveis dados adicionais específicos da ação auditada (ex: campos alterados,
     *     motivo de falha), em formato de mapa livre por não haver um schema fixo compartilhado
     *     entre os diferentes tipos de ação auditados
     */
    @Schema(description = "Item individual da trilha de auditoria")
    public record RegistroAuditavelItem(
            @Schema(description = "ID único do registro")
            UUID id,

            @Schema(description = "ID da organização onde a ação ocorreu")
            UUID grupoParoquialId,

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
