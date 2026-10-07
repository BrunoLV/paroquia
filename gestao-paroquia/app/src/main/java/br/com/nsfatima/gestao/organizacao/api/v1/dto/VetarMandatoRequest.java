package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Veto do Pároco a uma proposta de mandato; o motivo é obrigatório.
 */
@Schema(description = "Veto of a coordination mandate proposal")
public record VetarMandatoRequest(
        @Schema(description = "Reason for the veto")
        @NotBlank(message = "Reason is required")
        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String motivo
) {
}
