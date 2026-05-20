package br.com.nsfatima.gestao.iam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to reset user password")
public record PasswordResetRequest(
    @Schema(description = "New raw password", example = "newStrongPassword123")
    @NotBlank(message = "New password is required")
    String newPassword
) {
}
