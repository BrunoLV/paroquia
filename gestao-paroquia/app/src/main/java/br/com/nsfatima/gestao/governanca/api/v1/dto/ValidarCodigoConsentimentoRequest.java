package br.com.nsfatima.gestao.governanca.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Payload para validação do código OTP de 6 dígitos. */
public record ValidarCodigoConsentimentoRequest(
        @NotBlank(message = "O CPF é obrigatório.")
        String cpf,

        @NotBlank(message = "O código verificador é obrigatório.")
        @Pattern(regexp = "^\\d{6}$", message = "O código deve conter exatamente 6 dígitos numéricos.")
        String codigo
) {}
