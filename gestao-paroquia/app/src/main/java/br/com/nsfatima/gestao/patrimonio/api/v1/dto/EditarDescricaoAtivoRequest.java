package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import jakarta.validation.constraints.Size;

/**
 * Corpo da edição do ativo. Por imutabilidade estrutural (spec 3.2), apenas {@code descricao} é
 * aceita — qualquer outro campo enviado é ignorado pela desserialização.
 */
public record EditarDescricaoAtivoRequest(
        @Size(max = 4000) String descricao
) {
}
