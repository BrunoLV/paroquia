package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Solicitação de transferência de propriedade de um ativo patrimonial para outra organização —
 * exige justificativa por escrito para preservar rastreabilidade do porquê o ativo mudou de dono.
 */
public record TransferirAtivoRequest(
        @NotNull UUID novaGrupoParoquialId,
        @NotBlank @Size(min = 10, max = 2000) String justificativa
) {
}
