package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.UUID;

public class PastoralMovimentoOficial {
    private final UUID id;
    private String nome;
    private TipoOrganizacao tipo;

    public PastoralMovimentoOficial(UUID id, String nome, TipoOrganizacao tipo) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoOrganizacao getTipo() {
        return tipo;
    }

    public void update(String nome, TipoOrganizacao tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }
}
