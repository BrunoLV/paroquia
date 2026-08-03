package br.com.nsfatima.gestao.calendario.api.v1.dto.observacao;

import jakarta.validation.constraints.NotBlank;

/**
 * Corpo da requisição para atualizar o conteúdo textual de uma observação existente. O tipo da
 * observação não pode ser alterado após a criação.
 */
public record ObservacaoUpdateRequest(@NotBlank String conteudo) {
}
