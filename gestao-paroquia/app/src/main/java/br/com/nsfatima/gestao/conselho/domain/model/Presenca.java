package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;

import java.util.Objects;
import java.util.UUID;

/**
 * Presença nominal de uma pessoa na reunião. Guarda só o identificador do fiel: o nome é resolvido na
 * leitura e na exportação, para que a ata não duplique dado pessoal (LGPD). {@code funcao} é a função
 * exercida na reunião, como "Coordenador da Catequese", e é opcional.
 */
public record Presenca(UUID fielId, String funcao, SituacaoPresenca situacao) {

    private static final int FUNCAO_MAX = 80;

    public Presenca {
        Objects.requireNonNull(fielId, "O fiel da presença é obrigatório.");
        Objects.requireNonNull(situacao, "A situação da presença é obrigatória.");
        funcao = funcao == null || funcao.isBlank() ? null : funcao.trim();
        if (funcao != null && funcao.length() > FUNCAO_MAX) {
            throw new AtaInvalidaException("A função na reunião excede " + FUNCAO_MAX + " caracteres.");
        }
    }

    /** Indica se a pessoa esteve de fato na reunião. */
    public boolean presente() {
        return situacao == SituacaoPresenca.PRESENTE;
    }
}
