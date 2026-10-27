package br.com.nsfatima.gestao.iam.api.v1.dto;

import br.com.nsfatima.gestao.iam.api.v1.validation.SenhaValida;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Nova senha em texto puro para redefinição administrativa de acesso (o campo nunca é logado nem persistido
 * como veio — é criptografado antes de tocar o banco).
 */
@Schema(description = "Request to reset user password")
public record RedefinirSenhaRequest(
        @Schema(description = "New raw password", example = "newStrongPassword123")
        @SenhaValida(message = "A nova senha deve possuir no mínimo 8 caracteres e no máximo 72 bytes")
        String newPassword
) {
}
