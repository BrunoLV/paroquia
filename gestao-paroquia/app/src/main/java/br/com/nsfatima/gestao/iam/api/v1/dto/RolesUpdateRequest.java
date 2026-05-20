package br.com.nsfatima.gestao.iam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update user global roles")
public record RolesUpdateRequest(
    @Schema(description = "Comma separated roles", example = "ROLE_ADMIN,ROLE_USER")
    @NotBlank(message = "Roles are required")
    String roles
) {
}
