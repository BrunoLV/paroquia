package br.com.nsfatima.gestao.iam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to enable or disable a user")
public record UserStatusRequest(
    @Schema(description = "Whether the user should be enabled", example = "true")
    boolean enabled
) {
}
