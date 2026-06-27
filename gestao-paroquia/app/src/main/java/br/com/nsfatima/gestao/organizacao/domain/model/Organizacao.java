package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.UUID;

public class Organizacao {
    private final UUID id;
    private String nome;
    private TipoOrganizacao tipo;
    private String contato;
    private String coordenador;
    private String viceCoordenador;
    private boolean ativo;

    public Organizacao(UUID id, String nome, TipoOrganizacao tipo, String contato, String coordenador, String viceCoordenador, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.contato = contato;
        this.coordenador = coordenador;
        this.viceCoordenador = viceCoordenador;
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

    public String getCoordenador() {
        return coordenador;
    }

    public String getViceCoordenador() {
        return viceCoordenador;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void update(String nome, TipoOrganizacao tipo, String contato, String coordenador, String viceCoordenador, boolean ativo) {
        this.nome = nome;
        this.tipo = tipo;
        this.contato = contato;
        this.coordenador = coordenador;
        this.viceCoordenador = viceCoordenador;
        this.ativo = ativo;
    }
}
