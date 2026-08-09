package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;

import java.util.UUID;

/**
 * Resposta DTO representada por um nó de {@code UnidadeTerritorial}.
 */
public record UnidadeTerritorialResponse(
        UUID id,
        String nome,
        TipoUnidadeTerritorial tipo,
        String contato,
        boolean ativo,
        UUID parentId
) {
}
