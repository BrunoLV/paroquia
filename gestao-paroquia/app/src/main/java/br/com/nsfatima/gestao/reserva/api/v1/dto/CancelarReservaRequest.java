package br.com.nsfatima.gestao.reserva.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record CancelarReservaRequest(
        @NotBlank(message = "A justificativa de cancelamento é obrigatória.")
        String motivo,

        String itemNome
) {}
