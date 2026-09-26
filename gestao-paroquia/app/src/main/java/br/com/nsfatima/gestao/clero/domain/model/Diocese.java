package br.com.nsfatima.gestao.clero.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa a Igreja Particular (Diocese ou Arquidiocese) sob cuja jurisdição a paróquia se insere.
 */
public class Diocese {
    private final UUID id;
    private String nome;
    private String bispoDiocesano;
    private String cnpjMitra;
    private String cidade;
    private String uf;
    private String contato;
    private boolean ativo;

    public Diocese(UUID id, String nome, String bispoDiocesano, String cnpjMitra,
                   String cidade, String uf, String contato, boolean ativo) {
        this.id = Objects.requireNonNull(id, "ID da diocese é obrigatório.");
        this.nome = Objects.requireNonNull(nome, "Nome da diocese é obrigatório.").trim();
        if (this.nome.isBlank()) {
            throw new IllegalArgumentException("Nome da diocese não pode estar em branco.");
        }
        this.bispoDiocesano = Objects.requireNonNull(bispoDiocesano, "Bispo diocesano é obrigatório.").trim();
        if (this.bispoDiocesano.isBlank()) {
            throw new IllegalArgumentException("Bispo diocesano não pode estar em branco.");
        }
        this.cnpjMitra = cnpjMitra != null ? cnpjMitra.trim() : null;
        this.cidade = cidade != null ? cidade.trim() : null;
        this.uf = uf != null ? uf.trim().toUpperCase() : null;
        this.contato = contato != null ? contato.trim() : null;
        this.ativo = ativo;
    }

    public void atualizar(String nome, String bispoDiocesano, String cnpjMitra,
                          String cidade, String uf, String contato, boolean ativo) {
        this.nome = Objects.requireNonNull(nome, "Nome da diocese é obrigatório.").trim();
        if (this.nome.isBlank()) {
            throw new IllegalArgumentException("Nome da diocese não pode estar em branco.");
        }
        this.bispoDiocesano = Objects.requireNonNull(bispoDiocesano, "Bispo diocesano é obrigatório.").trim();
        if (this.bispoDiocesano.isBlank()) {
            throw new IllegalArgumentException("Bispo diocesano não pode estar em branco.");
        }
        this.cnpjMitra = cnpjMitra != null ? cnpjMitra.trim() : null;
        this.cidade = cidade != null ? cidade.trim() : null;
        this.uf = uf != null ? uf.trim().toUpperCase() : null;
        this.contato = contato != null ? contato.trim() : null;
        this.ativo = ativo;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getBispoDiocesano() {
        return bispoDiocesano;
    }

    public String getCnpjMitra() {
        return cnpjMitra;
    }

    public String getCidade() {
        return cidade;
    }

    public String getUf() {
        return uf;
    }

    public String getContato() {
        return contato;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
