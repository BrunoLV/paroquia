package br.com.nsfatima.gestao.local.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa um espaço físico da paróquia onde eventos, celebrações e reuniões podem ocorrer
 * (ex.: salas, salões paroquiais, quadras, auditórios).
 *
 * <p>Invariantes de domínio:
 * <ul>
 *   <li>Cada local pertence a uma organização de origem (Igreja Matriz ou Comunidade) ou é de uso geral;</li>
 *   <li>A capacidade máxima de ocupação é um número inteiro estritamente positivo;</li>
 *   <li>O ciclo de vida suporta exclusão lógica via {@code deletedAt} (soft delete) e ativação/inativação.</li>
 * </ul>
 */
public class Local {
    private final UUID id;
    private String nome;
    private String tipo;
    private String endereco;
    private EnderecoLocal enderecoLocal;
    private Integer capacidade;
    private Integer capacidadeMaxima;
    private String caracteristicas;
    private String descricao;
    private boolean ativo;
    private UUID grupoParoquialId;
    private String grupoParoquialNome;
    private Instant deletedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public Local(UUID id,
                 String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                 boolean ativo) {
        this(id, nome, tipo, endereco, capacidade, caracteristicas, ativo, null, null);
    }

    public Local(UUID id,
                 String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                 boolean ativo, UUID grupoParoquialId) {
        this(id, nome, tipo, endereco, capacidade, caracteristicas, ativo, grupoParoquialId, null);
    }

    public Local(UUID id,
                 String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                 boolean ativo, UUID grupoParoquialId, String grupoParoquialNome) {
        this(id, nome, tipo, endereco, null, capacidade, capacidade, caracteristicas, caracteristicas,
                ativo, grupoParoquialId, grupoParoquialNome, null, null, null);
    }

    public Local(UUID id,
                 String nome,
                 String tipo,
                 String endereco,
                 EnderecoLocal enderecoLocal,
                 Integer capacidade,
                 Integer capacidadeMaxima,
                 String caracteristicas,
                 String descricao,
                 boolean ativo,
                 UUID grupoParoquialId,
                 String grupoParoquialNome,
                 Instant deletedAt,
                 Instant createdAt,
                 Instant updatedAt) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.endereco = endereco;
        this.enderecoLocal = enderecoLocal != null ? enderecoLocal : EnderecoLocal.vazio();
        this.capacidade = capacidadeMaxima != null ? capacidadeMaxima : capacidade;
        this.capacidadeMaxima = this.capacidade;
        this.caracteristicas = descricao != null ? descricao : caracteristicas;
        this.descricao = this.caracteristicas;
        this.ativo = ativo;
        this.grupoParoquialId = grupoParoquialId;
        this.grupoParoquialNome = grupoParoquialNome;
        this.deletedAt = deletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public EnderecoLocal getEnderecoLocal() {
        return enderecoLocal;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public Integer getCapacidadeMaxima() {
        return capacidadeMaxima != null ? capacidadeMaxima : capacidade;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    }

    public String getDescricao() {
        return descricao != null ? descricao : caracteristicas;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public UUID getGrupoParoquialId() {
        return grupoParoquialId;
    }

    public UUID getIgrejaId() {
        return grupoParoquialId;
    }

    public String getGrupoParoquialNome() {
        return grupoParoquialNome;
    }

    public String getIgrejaNome() {
        return grupoParoquialNome;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isExcluido() {
        return deletedAt != null;
    }

    public void marcarExcluido() {
        this.deletedAt = Instant.now();
        this.ativo = false;
        this.updatedAt = Instant.now();
    }

    public void inativar() {
        this.ativo = false;
        this.updatedAt = Instant.now();
    }

    public void ativar() {
        this.ativo = true;
        this.updatedAt = Instant.now();
    }

    public void update(String nome,
                       String tipo, String endereco, Integer capacidade, String caracteristicas, boolean ativo) {
        update(nome, tipo, endereco, capacidade, caracteristicas, ativo, this.grupoParoquialId);
    }

    public void update(String nome,
                       String tipo, String endereco, Integer capacidade, String caracteristicas, boolean ativo,
                       UUID grupoParoquialId) {
        this.nome = nome;
        this.tipo = tipo;
        this.endereco = endereco;
        this.capacidade = capacidade;
        this.capacidadeMaxima = capacidade;
        this.caracteristicas = caracteristicas;
        this.descricao = caracteristicas;
        this.ativo = ativo;
        this.grupoParoquialId = grupoParoquialId;
        this.updatedAt = Instant.now();
    }

    public void updateCompleto(String nome,
                               String tipo,
                               String endereco,
                               EnderecoLocal enderecoLocal,
                               Integer capacidadeMaxima,
                               String descricao,
                               boolean ativo,
                               UUID igrejaId) {
        this.nome = nome;
        this.tipo = tipo;
        this.endereco = endereco;
        this.enderecoLocal = enderecoLocal != null ? enderecoLocal : EnderecoLocal.vazio();
        this.capacidade = capacidadeMaxima;
        this.capacidadeMaxima = capacidadeMaxima;
        this.caracteristicas = descricao;
        this.descricao = descricao;
        this.ativo = ativo;
        this.grupoParoquialId = igrejaId;
        this.updatedAt = Instant.now();
    }
}
