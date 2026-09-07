package br.com.nsfatima.gestao.clero.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

/** Alterações de contato e residência submetidas pelo próprio clérigo para moderação. */
public record SolicitarAjusteClericalRequest(
        @NotBlank String email,
        @NotBlank String celular,
        String telefoneFixo,
        @NotBlank String endereco) {
}
