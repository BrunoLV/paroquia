package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;

import java.util.Objects;
import java.util.UUID;

/**
 * Presença nominal de uma pessoa na reunião. Guarda só o identificador do fiel: o nome é resolvido na
 * leitura e na exportação, para que a ata não duplique dado pessoal (LGPD). {@code funcao} é a função
 * exercida na reunião, uma das {@link FuncaoPresenca}, e é opcional; só quem esteve presente exerce função.
 */
public record Presenca(UUID fielId, String funcao, SituacaoPresenca situacao) {

    public Presenca {
        Objects.requireNonNull(fielId, "O fiel da presença é obrigatório.");
        Objects.requireNonNull(situacao, "A situação da presença é obrigatória.");
        funcao = funcao == null || funcao.isBlank() ? null : funcao.trim();
        if (funcao != null) {
            FuncaoPresenca.doRotulo(funcao);
            if (situacao != SituacaoPresenca.PRESENTE) {
                throw new AtaInvalidaException("Só quem esteve presente exerce função na reunião.");
            }
        }
    }

    /** Indica se a pessoa esteve de fato na reunião. */
    public boolean presente() {
        return situacao == SituacaoPresenca.PRESENTE;
    }
}
