package br.com.nsfatima.gestao.governanca.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Requisição de transição de status de um ano paroquial (ex.: fechar ou concluir o ano corrente), usada pelo
 * {@code AnoParoquialController} para acionar a máquina de estados de governança do calendário anual.
 */
@Schema(description = "Requisição para atualizar o status de um ano paroquial")
public record AtualizarAnoParoquialRequest(
        @NotBlank
        @Schema(description = "Novo status (EM_PLANEJAMENTO, FECHADO, CONCLUIDO)")
        String status) {
}
