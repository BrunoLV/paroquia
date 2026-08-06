package br.com.nsfatima.gestao.membro.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Payload para promover um {@code Fiel} já membro ativo de uma organização a um papel de liderança. Se o
 * Fiel ainda não possuir {@code Usuario}, {@code username}/{@code password} são usados para criar um novo
 * login vinculado a ele; se já possuir, esses campos são ignorados.
 */
public record PromoverACoordenadorRequest(
        @NotNull UUID fielId,
        @NotNull UUID grupoParoquialId,
        @NotBlank String papel,
        String username,
        String password
) {
}
