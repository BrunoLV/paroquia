package br.com.nsfatima.gestao.iam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para solicitação de alteração da própria senha por usuário autenticado.
 */
@Schema(description = "Payload para alteração da própria senha por usuário autenticado")
public record AlterarMinhaSenhaRequest(
        @Schema(description = "Senha atual do usuário", example = "SenhaAtual@123")
        @NotBlank(message = "A senha atual é obrigatória")
        String senhaAtual,

        @Schema(description = "Nova senha desejada", example = "NovaSenha@2026")
        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 6, max = 100, message = "A nova senha deve possuir entre 6 e 100 caracteres")
        String novaSenha
) {}
