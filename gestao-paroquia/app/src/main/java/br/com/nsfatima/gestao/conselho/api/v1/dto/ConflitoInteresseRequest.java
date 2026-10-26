package br.com.nsfatima.gestao.conselho.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Declaração de conflito de interesses de um participante sobre uma deliberação. */
public record ConflitoInteresseRequest(
        @NotNull UUID declaranteFielId,
        @NotBlank @Size(max = 500) String descricao) {
}
