package br.com.nsfatima.gestao.governanca.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para atualizar o status de um ano paroquial")
public record UpdateAnoParoquialRequest(
        @NotBlank
        @Schema(description = "Novo status (PLANEJAMENTO, FECHADO)")
        String status) {
}
