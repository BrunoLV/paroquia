package br.com.nsfatima.gestao.iam.api.v1.dto;

import br.com.nsfatima.gestao.iam.api.v1.validation.SenhaValida;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload para solicitação de alteração da própria senha por usuário autenticado.
 */
@Schema(description = "Payload para alteração da própria senha por usuário autenticado")
public record AlterarMinhaSenhaRequest(
        @Schema(description = "Senha atual do usuário", example = "SenhaAtual@123")
        @NotBlank(message = "A senha atual é obrigatória")
        String senhaAtual,

        @Schema(description = "Nova senha desejada", example = "NovaSenha@2026")
        @SenhaValida(message = "A nova senha deve possuir no mínimo 8 caracteres e no máximo 72 bytes")
        String novaSenha
) {}
