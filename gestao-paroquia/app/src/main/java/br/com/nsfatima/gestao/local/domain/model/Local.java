package br.com.nsfatima.gestao.local.domain.model;

import java.util.UUID;

/**
 * Representa um espaço físico da paróquia onde eventos podem ocorrer.
 */
public class Local {
    private final UUID id;
    private String nome;
    private String tipo;
    private String endereco;
    private Integer capacidade;
    private String caracteristicas;
    private boolean ativo;
    private UUID organizacaoId;
    private String organizacaoNome;

    public Local(UUID id,
                 String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                 boolean ativo) {

        this(id, nome, tipo, endereco, capacidade, caracteristicas, ativo, null, null);
    }

    public Local(UUID id,
                 String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                 boolean ativo, UUID organizacaoId) {

        this(id, nome, tipo, endereco, capacidade, caracteristicas, ativo, organizacaoId, null);
    }

    public Local(UUID id,
                 String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                 boolean ativo, UUID organizacaoId, String organizacaoNome) {

        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.endereco = endereco;
        this.capacidade = capacidade;
        this.caracteristicas = caracteristicas;
        this.ativo = ativo;
        this.organizacaoId = organizacaoId;
        this.organizacaoNome = organizacaoNome;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public String getEndereco() {
        return endereco;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public String getOrganizacaoNome() {
        return organizacaoNome;
    }

    public void update(String nome,
                       String tipo, String endereco, Integer capacidade, String caracteristicas, boolean ativo) {

        update(nome, tipo, endereco, capacidade, caracteristicas, ativo, this.organizacaoId);
    }

    public void update(String nome,
                       String tipo, String endereco, Integer capacidade, String caracteristicas, boolean ativo,
                       UUID organizacaoId) {

        this.nome = nome;
        this.tipo = tipo;
        this.endereco = endereco;
        this.capacidade = capacidade;
        this.caracteristicas = caracteristicas;
        this.ativo = ativo;
        this.organizacaoId = organizacaoId;
    }
}
