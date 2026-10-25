package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.ResultadoDeliberacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Deliberação de um item de pauta; a numeração segue a posição na lista. */
public record DeliberacaoRequest(
        @NotBlank @Size(max = 4000) String texto,
        @NotNull ResultadoDeliberacao resultado,
        @Valid VotacaoRequest votacao,
        List<@Valid ConflitoInteresseRequest> conflitos) {
}
