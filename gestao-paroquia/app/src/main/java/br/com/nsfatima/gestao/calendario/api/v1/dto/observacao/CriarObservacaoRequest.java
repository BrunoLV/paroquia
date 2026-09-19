package br.com.nsfatima.gestao.calendario.api.v1.dto.observacao;

import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo da requisição para registrar uma nova observação em um evento.
 */
public record CriarObservacaoRequest(
        @NotNull TipoObservacaoInput tipo,
        @NotBlank String conteudo) {
}
