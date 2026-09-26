package br.com.nsfatima.gestao.clero.api.v1.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record GovernoEclesiasticoRequest(
        @NotNull(message = "A diocese vinculada é obrigatória")
        UUID dioceseId,

        UUID parocoId,

        LocalDate dataPosseParoco,

        String decretoErecaoParoquia,

        LocalDate dataInstalacaoParoquia,

        String livroTomboNumero
) {
}
