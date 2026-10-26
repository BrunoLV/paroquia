package br.com.nsfatima.gestao.conselho.api.v1.dto;

import jakarta.validation.constraints.Min;

/** Apuração consultiva de votos de uma deliberação. */
public record VotacaoRequest(@Min(0) int favor, @Min(0) int contra, @Min(0) int abstencoes) {
}
