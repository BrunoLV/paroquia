package br.com.nsfatima.gestao.iam.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Dados para criação administrativa de um usuário (via {@code POST /api/v1/usuarios}, restrito a
 * {@code ROLE_ADMIN}) — único fluxo de criação de {@code Usuario}, já que o autocadastro público foi
 * removido. Exige o {@code fielId} de um {@code Fiel} já cadastrado; o usuário criado já nasce habilitado
 * e sem o papel padrão {@code ROLE_USER} aplicado automaticamente.
 */
public record CreateUsuarioRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull UUID fielId
) {
}
