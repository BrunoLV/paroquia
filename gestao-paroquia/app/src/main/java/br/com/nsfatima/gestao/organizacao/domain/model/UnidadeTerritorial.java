package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.UUID;

/**
 * Representa um nó da hierarquia territorial/estrutural da paróquia (Clero, Matriz, Comunidade) —
 * diferente de {@link GrupoParoquial}, não é um agrupamento de fiéis, e sim a estrutura que hospeda os
 * grupos paroquiais e é referenciada como escopo territorial em vínculos, eventos e outros recursos.
 */
public class UnidadeTerritorial {
    private final UUID id;
    private String nome;
    private TipoUnidadeTerritorial tipo;
    private String contato;
    private boolean ativo;
    private UUID parentId;

    public UnidadeTerritorial(UUID id, String nome, TipoUnidadeTerritorial tipo, String contato,
                               boolean ativo, UUID parentId) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.contato = contato;
        this.ativo = ativo;
        this.parentId = parentId;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoUnidadeTerritorial getTipo() {
        return tipo;
    }

    public void setTipo(TipoUnidadeTerritorial tipo) {
        this.tipo = tipo;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }
}
