package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Payload de requisição para criação ou atualização de {@code UnidadeTerritorial}.
 */
public record UnidadeTerritorialRequest(
        @NotBlank(message = "O nome da unidade territorial é obrigatório")
        String nome,

        @NotNull(message = "O tipo da unidade territorial é obrigatório")
        TipoUnidadeTerritorial tipo,

        String contato,

        boolean ativo,

        UUID parentId
) {
}
