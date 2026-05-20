package br.com.nsfatima.gestao.aprovacao.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

@Schema(description = "Resposta após a decisão de uma aprovação")
public record AprovacaoDecisionResponse(
                @Schema(description = "ID único da aprovação")
                UUID id,

                @Schema(description = "Novo status da aprovação")
                String status,

                @Schema(description = "Detalhes da execução da ação associada")
                ActionExecution actionExecution) {

        @Schema(description = "Detalhes da execução da ação")
        public record ActionExecution(
                        @Schema(description = "Resultado da ação (SUCCESS ou FAILURE)")
                        String outcome,

                        @Schema(description = "ID do recurso afetado (evento)")
                        UUID eventoId,

                        @Schema(description = "Novo status do recurso afetado")
                        String eventStatus,

                        @Schema(description = "Código de erro em caso de falha")
                        String errorCode) {

                @JsonProperty("targetResourceId")
                public UUID targetResourceId() {
                        return eventoId;
                }

                @JsonProperty("targetStatus")
                public String targetStatus() {
                        return eventStatus;
                }
        }
}
