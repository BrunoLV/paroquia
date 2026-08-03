package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Pedido de alocação de um ativo patrimonial.
 *
 * @param eventoId evento de destino; quando omitido, a alocação é confirmada diretamente sem
 *     vínculo a um evento e sem checagem de empréstimo entre organizações
 */
public record AlocarAtivoRequest(
        UUID eventoId,
        @NotBlank @Size(min = 10, max = 4000) String justificativaAlocacao,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataFim
) {
}
