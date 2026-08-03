package br.com.nsfatima.gestao.iam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Novo estado de habilitação de um usuário. Quando {@code enabled} é {@code false}, o usuário não consegue
 * mais autenticar, mesmo com credenciais válidas — usado para bloquear acesso sem excluir a conta.
 */
@Schema(description = "Request to enable or disable a user")
public record UsuarioStatusRequest(
        @Schema(description = "Whether the user should be enabled", example = "true")
        boolean enabled
) {
}
