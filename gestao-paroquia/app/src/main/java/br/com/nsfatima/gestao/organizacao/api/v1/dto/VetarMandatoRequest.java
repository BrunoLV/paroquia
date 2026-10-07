package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Veto do Pároco a uma proposta de mandato; o motivo é obrigatório.
 */
@Schema(description = "Veto a uma proposta de mandato de coordenação")
public record VetarMandatoRequest(
        @Schema(description = "Motivo do veto")
        @NotBlank(message = "O motivo é obrigatório")
        @Size(max = 500, message = "O motivo não pode exceder 500 caracteres")
        String motivo
) {
}
