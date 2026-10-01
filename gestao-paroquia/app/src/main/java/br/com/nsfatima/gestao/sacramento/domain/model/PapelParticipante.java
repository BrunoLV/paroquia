package br.com.nsfatima.gestao.sacramento.domain.model;

/** Função de uma pessoa no assento; define se o vínculo tardio aponta a Fiel ou a Clérigo. */
public enum PapelParticipante {
    BATIZANDO(false),
    CRISMANDO(false),
    CONJUGE(false),
    ORDENADO(true),
    PAI(false),
    MAE(false),
    ADOTANTE(false),
    PADRINHO(false),
    MADRINHA(false),
    TESTEMUNHA(false),
    MINISTRO_CELEBRANTE(true),
    MINISTRO_ASSISTENTE(true),
    BISPO_ORDENANTE(true);

    private final boolean clerical;

    PapelParticipante(boolean clerical) {
        this.clerical = clerical;
    }

    /**
     * Indica o tipo de cadastro ao qual o vínculo tardio se refere.
     *
     * @return {@code true} se o papel referencia um cadastro de clérigo
     */
    public boolean aceitaClerigo() {
        return clerical;
    }
}
