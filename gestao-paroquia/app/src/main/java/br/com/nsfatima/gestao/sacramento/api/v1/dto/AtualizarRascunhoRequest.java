package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Novo teor integral do rascunho. */
public record AtualizarRascunhoRequest(@NotNull @Valid TeorAssentoRequest teor) {
}
