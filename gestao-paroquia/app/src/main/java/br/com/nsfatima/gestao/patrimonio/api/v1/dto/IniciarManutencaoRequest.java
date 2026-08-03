package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Solicitação para colocar um ativo patrimonial em manutenção por um período determinado,
 * tornando-o indisponível para alocação enquanto durar — exige motivo justificado.
 */
public record IniciarManutencaoRequest(
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim,
        @NotBlank @Size(min = 10, max = 4000) String motivo
) {
}
