package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Ressalva formal de retificação material, atestada pelo Pároco. */
public record RetificacaoRequest(
        @NotBlank @Size(max = 128) String campo,
        String valorOriginal,
        @NotBlank String valorCorrigido,
        @NotBlank String justificativa) {
}
