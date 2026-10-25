package br.com.nsfatima.gestao.conselho.domain.model;

/**
 * Como a presença é registrada. Colegiados com membros definidos usam lista nominal dos membros;
 * reuniões abertas a convidados usam lista nominal livre; assembleias numerosas registram apenas o total
 * de participantes.
 */
public enum ModoPresenca {
    NOMINAL_MEMBROS,
    NOMINAL_LIVRE,
    QUANTITATIVA;

    /** Indica se a presença é uma lista de pessoas, e não apenas uma contagem. */
    public boolean nominal() {
        return this != QUANTITATIVA;
    }
}
