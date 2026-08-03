package br.com.nsfatima.gestao.conselho.domain.model;

/**
 * Situação de homologação de uma ata/decisão de conselho (CPC/CPP) no fluxo de aprovação.
 * Toda decisão nasce {@link #PENDENTE_HOMOLOGACAO} e só se torna válida/oficial ao ser
 * {@link #HOMOLOGADO} pela autoridade competente (Pároco, Admin ou Mesa CPP); pode também
 * ser {@link #VETADO}, caso em que um motivo de veto passa a ser obrigatório.
 */
public enum StatusHomologacao {
    PENDENTE_HOMOLOGACAO("Pendente de Homologação"),
    HOMOLOGADO("Homologado"),
    VETADO("Vetado");

    private final String descricao;

    StatusHomologacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
