package br.com.nsfatima.gestao.conselho.api.v1.dto;

import java.util.UUID;

/** Conflito de interesses declarado, com o nome do declarante resolvido na leitura. */
public record ConflitoInteresseResponse(UUID declaranteFielId, String declaranteNome, String descricao) {
}
