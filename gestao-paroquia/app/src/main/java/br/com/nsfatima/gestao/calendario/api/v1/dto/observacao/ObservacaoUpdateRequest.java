package br.com.nsfatima.gestao.calendario.api.v1.dto.observacao;

import jakarta.validation.constraints.NotBlank;

public record ObservacaoUpdateRequest(@NotBlank String conteudo) {
}
