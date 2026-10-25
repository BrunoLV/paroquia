package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.SituacaoPresenca;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Presença de uma pessoa na reunião; só o identificador do fiel, o nome é resolvido pelo servidor. */
public record PresencaItemRequest(
        @NotNull UUID fielId,
        @Size(max = 80) String funcao,
        @NotNull SituacaoPresenca situacao) {
}
