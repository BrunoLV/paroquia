package br.com.nsfatima.gestao.conselho.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateCpcDecisaoRequest(
        @NotBlank @Size(max = 160) String titulo,
        @NotBlank @Size(max = 4000) String pautaDescricao,
        @NotNull LocalDate dataReuniao,
        @NotNull UUID comunidadeId
) {
}
