package br.com.nsfatima.gestao.fiel.api.v1.dto;

import jakarta.validation.constraints.NotNull;

/** Comando explícito para o ciclo de vida do cadastro; não se mistura à edição cadastral. */
public record AlterarStatusFielRequest(@NotNull Boolean ativo) {
}
