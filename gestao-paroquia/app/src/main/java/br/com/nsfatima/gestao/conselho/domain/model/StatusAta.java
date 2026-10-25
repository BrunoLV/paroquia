package br.com.nsfatima.gestao.conselho.domain.model;

/**
 * Ciclo de vida da ata. {@code HOMOLOGADA} e {@code REGISTRADA} são estados selados: o conteúdo deixa de
 * ser editável e qualquer correção exige ata retificadora (RN-CON-03). {@code REGISTRADA} é o desfecho dos
 * tipos de reunião que não dependem de homologação do Pároco.
 */
public enum StatusAta {
    RASCUNHO,
    PENDENTE_HOMOLOGACAO,
    HOMOLOGADA,
    VETADA,
    REGISTRADA;

    /** Indica se a ata já produz efeito e não admite mais edição de conteúdo. */
    public boolean selada() {
        return this == HOMOLOGADA || this == REGISTRADA;
    }
}
