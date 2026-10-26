package br.com.nsfatima.gestao.conselho.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Motivo do veto da ata pelo Pároco. */
public record VetarAtaRequest(@NotBlank @Size(max = 2000) String motivo) {
}
