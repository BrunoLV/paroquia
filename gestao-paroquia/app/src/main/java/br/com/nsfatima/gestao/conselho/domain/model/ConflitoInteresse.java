package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;

import java.util.Objects;
import java.util.UUID;

/**
 * Declaração de conflito de interesses feita por um participante sobre uma deliberação (relevante sobretudo
 * em assuntos econômicos). O declarante fica de fora da contagem de votos daquela deliberação.
 */
public record ConflitoInteresse(UUID declaranteFielId, String descricao) {

    private static final int DESCRICAO_MAX = 500;

    public ConflitoInteresse {
        Objects.requireNonNull(declaranteFielId, "O declarante do conflito de interesses é obrigatório.");
        if (descricao == null || descricao.isBlank()) {
            throw new AtaInvalidaException("A descrição do conflito de interesses é obrigatória.");
        }
        descricao = descricao.trim();
        if (descricao.length() > DESCRICAO_MAX) {
            throw new AtaInvalidaException("A descrição do conflito excede " + DESCRICAO_MAX + " caracteres.");
        }
    }
}
