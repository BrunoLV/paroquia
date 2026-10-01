package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/** Lavratura de um novo rascunho; {@code visibilidade} omitida equivale a livro comum. */
public record LavrarAssentoRequest(
        @NotNull TipoSacramento tipo,
        Visibilidade visibilidade,
        @NotNull @Valid TeorAssentoRequest teor) {
}
