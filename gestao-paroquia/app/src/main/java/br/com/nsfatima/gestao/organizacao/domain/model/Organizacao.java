package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.UUID;

public class Organizacao {
    private final UUID id;
    private String nome;
    private TipoOrganizacao tipo;
    private String contato;
    private boolean ativo;

    public Organizacao(UUID id, String nome, TipoOrganizacao tipo, String contato, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.contato = contato;
        this.ativo = ativo;
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

    public String getContato() {
        return contato;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void update(String nome, TipoOrganizacao tipo, String contato, boolean ativo) {
        this.nome = nome;
        this.tipo = tipo;
        this.contato = contato;
        this.ativo = ativo;
    }
}
