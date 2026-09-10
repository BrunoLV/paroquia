package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Solicitação de envio de um ativo para manutenção: relatório do reparo e janela de
 * indisponibilidade (início e previsão de término).
 */
public record EnviarParaManutencaoRequest(
        @NotBlank @Size(min = 10, max = 4000) String descricaoReparo,
        @NotNull Instant dataInicio,
        @NotNull Instant dataFimPrevista
) {
}
