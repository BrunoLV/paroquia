package br.com.nsfatima.gestao.iam.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados para criação administrativa de um usuário (via {@code POST /api/v1/usuarios}, restrito a
 * {@code ROLE_ADMIN}). Diferente do autocadastro público, o usuário criado por este fluxo já nasce habilitado
 * e sem o papel padrão {@code ROLE_USER} aplicado automaticamente.
 */
public record CreateUsuarioRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
