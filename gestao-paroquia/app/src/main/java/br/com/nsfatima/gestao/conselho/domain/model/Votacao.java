package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;

/**
 * Apuração de votos de uma deliberação. É consultiva (Cân. 536 §2): registra a posição do colegiado para
 * orientar o Pároco, sem valor decisório próprio.
 */
public record Votacao(int favor, int contra, int abstencoes) {

    public Votacao {
        if (favor < 0 || contra < 0 || abstencoes < 0) {
            throw new AtaInvalidaException("A contagem de votos não pode ser negativa.");
        }
    }

    /** Total de votos computados, incluindo abstenções. */
    public int total() {
        return favor + contra + abstencoes;
    }
}
