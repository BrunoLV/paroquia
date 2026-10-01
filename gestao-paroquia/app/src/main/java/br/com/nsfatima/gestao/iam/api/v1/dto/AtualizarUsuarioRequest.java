package br.com.nsfatima.gestao.iam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Payload para atualização parcial de usuário (PATCH).
 * Permite alteração de credencial básica (username) e habilitação (enabled).
 */
@Schema(description = "Payload para atualização parcial de usuário")
public record AtualizarUsuarioRequest(
        @Schema(description = "Nome de usuário ou login para acesso", example = "maria.silva")
        @Size(min = 3, max = 50, message = "O username deve possuir entre 3 e 50 caracteres")
        String username,

        @Schema(description = "Estado de habilitação da conta no sistema", example = "true")
        Boolean enabled
) {}
