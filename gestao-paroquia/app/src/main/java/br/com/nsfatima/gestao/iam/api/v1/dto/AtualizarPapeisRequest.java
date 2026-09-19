package br.com.nsfatima.gestao.iam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Nova lista de papéis (roles) globais de um usuário, como string separada por vírgulas (ex.:
 * {@code "ROLE_ADMIN,ROLE_USER"}). Substitui integralmente os papéis globais atuais — não é um incremento.
 */
@Schema(description = "Request to update user global roles")
public record AtualizarPapeisRequest(
        @Schema(description = "Comma separated roles", example = "ROLE_ADMIN,ROLE_USER")
        @NotBlank(message = "Roles are required")
        String roles
) {
}
