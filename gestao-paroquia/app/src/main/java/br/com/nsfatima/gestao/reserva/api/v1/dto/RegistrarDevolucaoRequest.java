package br.com.nsfatima.gestao.reserva.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

public record RegistrarDevolucaoRequest(
        @NotBlank(message = "O checklist/relatório de conferência de avarias é obrigatório.")
        String checkAvarias
) {}
