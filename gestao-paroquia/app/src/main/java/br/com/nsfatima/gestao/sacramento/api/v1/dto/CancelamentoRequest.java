package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

/** Cancelamento de termo em duplicidade ou com erro formal grave. */
public record CancelamentoRequest(@NotBlank String motivo) {
}
