package br.com.nsfatima.gestao.conselho.domain.model;

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
