package br.com.nsfatima.gestao.membro.api.v1.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload para inscrever um membro em uma organização adicional, registrando a data de início da participação.
 */
public record ParticipacaoRequest(
        @NotNull UUID organizacaoId,
        @NotNull LocalDate dataInicio) {
}
