package br.com.nsfatima.gestao.membro.api.v1.dto;

import br.com.nsfatima.gestao.iam.api.v1.validation.SenhaValidaValidator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
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
        @NotNull UUID organizacaoId,
        @NotBlank String papel,
        String username,
        String password
) {

    @JsonIgnore
    @AssertTrue(message = "A senha deve possuir no mínimo 8 caracteres e no máximo 72 bytes")
    public boolean isPasswordValida() {
        return password == null || SenhaValidaValidator.atende(password);
    }
}
