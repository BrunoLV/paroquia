package br.com.nsfatima.gestao.membro.api.v1.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ParticipacaoRequest(
        @NotNull UUID organizacaoId,
        @NotNull LocalDate dataInicio) {
}
