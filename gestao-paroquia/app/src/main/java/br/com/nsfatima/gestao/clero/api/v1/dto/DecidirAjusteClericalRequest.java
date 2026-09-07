package br.com.nsfatima.gestao.clero.api.v1.dto;

import jakarta.validation.constraints.NotNull;

/** Decisão administrativa sobre uma solicitação de ajuste clerical. */
public record DecidirAjusteClericalRequest(@NotNull Boolean aprovar, String justificativa) {
}
