package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TransferirAtivoRequest(
        @NotNull UUID novaOrganizacaoId,
        @NotBlank @Size(min = 10, max = 2000) String justificativa
) {
}
