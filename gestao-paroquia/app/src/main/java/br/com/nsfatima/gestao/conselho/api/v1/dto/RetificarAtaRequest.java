package br.com.nsfatima.gestao.conselho.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Justificativa formal da ata retificadora. */
public record RetificarAtaRequest(@NotBlank @Size(max = 2000) String justificativa) {
}
