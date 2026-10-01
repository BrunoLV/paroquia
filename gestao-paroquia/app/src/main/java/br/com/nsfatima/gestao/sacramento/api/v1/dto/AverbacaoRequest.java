package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import br.com.nsfatima.gestao.sacramento.domain.model.TipoEventoAverbacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Digitação de nota marginal no assento de Batismo. */
public record AverbacaoRequest(
        @NotNull TipoEventoAverbacao tipoEvento,
        @NotNull LocalDate dataAverbacao,
        @Size(max = 128) String numeroDocumento,
        @NotBlank String teorNota,
        Boolean reservada) {
}
