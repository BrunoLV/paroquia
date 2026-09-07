package br.com.nsfatima.gestao.fiel.api.v1.dto;

import jakarta.validation.constraints.NotNull;

/** Campos de contato/endereço que o fiel pode propor. */
public record SolicitarAjusteCadastralRequest(
        String email,
        String celular,
        String telefoneFixo,
        @NotNull EnderecoDto endereco) {
}
