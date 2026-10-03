package br.com.nsfatima.gestao.calendario.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Payload de requisição para atualização das cores da identidade visual da paróquia.
 */
@Schema(description = "Requisição para atualização das cores da identidade visual da paróquia")
public record AtualizarIdentidadeVisualRequest(
        @Schema(description = "Cor primária da identidade visual em hexadecimal (#RRGGBB)", example = "#1A237E")
        @NotBlank(message = "Cor primária é obrigatória.")
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Cor primária deve estar no formato hexadecimal '#RRGGBB'.")
        String corPrimariaHex,

        @Schema(description = "Cor secundária da identidade visual em hexadecimal (#RRGGBB)", example = "#808080")
        @NotBlank(message = "Cor secundária é obrigatória.")
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Cor secundária deve estar no formato hexadecimal '#RRGGBB'.")
        String corSecundariaHex
) {
}
