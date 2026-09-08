package br.com.nsfatima.gestao.reserva.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record RejeitarReservaRequest(
        @NotBlank(message = "O motivo da rejeição é obrigatório.")
        String motivo
) {}
