package br.com.nsfatima.gestao.membro.api.v1.dto;

import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record ParticipacaoRequest(
        @NotNull UUID organizacaoId,
        @NotNull LocalDate dataInicio) {
}
