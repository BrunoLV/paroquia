package br.com.nsfatima.gestao.conselho.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import java.util.List;

/** Item da ordem do dia; a numeração segue a posição na lista. */
public record ItemPautaRequest(
        @NotBlank @Size(max = 160) String titulo,
        @Size(max = 4000) String relato,
        List<@Valid DeliberacaoRequest> deliberacoes) {
}
