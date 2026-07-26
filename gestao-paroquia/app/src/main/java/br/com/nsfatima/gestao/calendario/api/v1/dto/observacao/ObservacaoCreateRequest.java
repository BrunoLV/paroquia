package br.com.nsfatima.gestao.calendario.api.v1.dto.observacao;

import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ObservacaoCreateRequest(
        @NotNull TipoObservacaoInput tipo,
        @NotBlank String conteudo) {
}
