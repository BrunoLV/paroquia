package br.com.nsfatima.gestao.iam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Nova senha em texto puro para redefinição administrativa de acesso (o campo nunca é logado nem persistido
 * como veio — é criptografado antes de tocar o banco).
 */
@Schema(description = "Request to reset user password")
public record RedefinirSenhaRequest(
        @Schema(description = "New raw password", example = "newStrongPassword123")
        @NotBlank(message = "New password is required")
        String newPassword
) {
}
