package br.com.nsfatima.gestao.iam.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload para solicitação anônima de redefinição de senha na tela de login.
 */
@Schema(description = "Payload para solicitação de redefinição de acesso")
public record SolicitarResetSenhaRequest(
        @Schema(description = "Nome de usuário ou e-mail cadastrado", example = "maria.silva")
        @NotBlank(message = "O nome de usuário é obrigatório")
        @Size(min = 3, max = 150, message = "O identificador deve possuir entre 3 e 150 caracteres")
        String username
) {}
