package br.com.nsfatima.gestao.governanca.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

/** Solicitação de envio de código OTP para validação de consentimento. */
public record SolicitarCodigoConsentimentoRequest(
        @NotBlank(message = "O CPF é obrigatório.")
        String cpf
) {}
