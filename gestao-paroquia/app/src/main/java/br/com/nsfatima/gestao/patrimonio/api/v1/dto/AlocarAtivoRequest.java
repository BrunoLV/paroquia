package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record AlocarAtivoRequest(
        UUID eventoId,
        @NotBlank @Size(min = 10, max = 4000) String justificativaAlocacao,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim
) {
}
