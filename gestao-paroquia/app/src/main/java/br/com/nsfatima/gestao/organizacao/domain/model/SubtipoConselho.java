package br.com.nsfatima.gestao.organizacao.domain.model;

/**
 * Natureza do conselho paroquial. {@code CPP} (Cân. 536) e {@code CPC} (conselho pastoral comunitário) são os
 * espaços de comunhão e planejamento pastoral aos quais pastorais, movimentos e serviços se articulam;
 * {@code CAEP} (Cân. 537) é o conselho para assuntos econômicos, de natureza temporal, e portanto nunca é
 * organização-mãe de um organismo pastoral.
 */
public enum SubtipoConselho {
    CPP,
    CPC,
    CAEP;

    /**
     * Indica se este conselho é um foro pastoral (sinodal) apto a acolher pastorais, movimentos e serviços.
     *
     * @return {@code true} para {@code CPP} e {@code CPC}
     */
    public boolean isPastoral() {
        return this != CAEP;
    }
}
