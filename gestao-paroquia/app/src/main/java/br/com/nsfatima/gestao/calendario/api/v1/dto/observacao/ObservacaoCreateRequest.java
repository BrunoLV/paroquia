package br.com.nsfatima.gestao.calendario.api.v1.dto.observacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;

public record ObservacaoCreateRequest(
                @NotNull TipoObservacaoInput tipo,
                @NotBlank String conteudo) {
}
