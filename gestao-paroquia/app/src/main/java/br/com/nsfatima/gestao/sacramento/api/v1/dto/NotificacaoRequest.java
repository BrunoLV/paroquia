package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Registro de notificação canônica à paróquia de batismo. */
public record NotificacaoRequest(
        @NotBlank @Size(max = 255) String destinatarioParoquia,
        @NotNull LocalDate emitidaEm,
        LocalDate enviadaEm) {
}
