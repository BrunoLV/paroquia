package br.com.nsfatima.gestao.local.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa um espaço físico paroquial (salas, salões, quadras, auditórios) vinculado a uma
 * Igreja Matriz ou Comunidade, apto a receber eventos, celebrações e formações.
 *
 * <p>Invariantes de domínio:
 * <ul>
 *   <li>Cada local pertence opcionalmente a uma paróquia/comunidade de origem ({@code igrejaId});</li>
 *   <li>A capacidade máxima de pessoas é um número inteiro estritamente positivo;</li>
 *   <li>O endereço é modelado através do Value Object imutável {@link EnderecoLocal};</li>
 *   <li>O ciclo de vida suporta exclusão lógica ({@code deletedAt}) e alteração de status ativo.</li>
 * </ul>
 */
public class Local {
    private final UUID id;
    private String nome;
    private String tipo;
    private EnderecoLocal endereco;
    private Integer capacidadeMaxima;
    private String descricao;
    private boolean ativo;
    private UUID igrejaId;
    private String igrejaNome;
    private Instant deletedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public Local(UUID id,
                 String nome,
                 String tipo,
                 EnderecoLocal endereco,
                 Integer capacidadeMaxima,
                 String descricao,
                 boolean ativo,
                 UUID igrejaId) {
        this(id, nome, tipo, endereco, capacidadeMaxima, descricao, ativo, igrejaId, null, null, Instant.now(), Instant.now());
    }

    public Local(UUID id,
                 String nome,
                 String tipo,
                 EnderecoLocal endereco,
                 Integer capacidadeMaxima,
                 String descricao,
                 boolean ativo,
                 UUID igrejaId,
                 String igrejaNome,
                 Instant deletedAt,
                 Instant createdAt,
                 Instant updatedAt) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.endereco = endereco != null ? endereco : EnderecoLocal.vazio();
        this.capacidadeMaxima = capacidadeMaxima;
        this.descricao = descricao;
        this.ativo = ativo;
        this.igrejaId = igrejaId;
        this.igrejaNome = igrejaNome;
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

    public EnderecoLocal getEndereco() {
        return endereco;
    }

    public Integer getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public UUID getIgrejaId() {
        return igrejaId;
    }

    public String getIgrejaNome() {
        return igrejaNome;
    }

    /**
     * Alias de interoperabilidade para contextos externos que referenciam o ID da organização subordinada.
     */
    public UUID getGrupoParoquialId() {
        return igrejaId;
    }

    /**
     * Alias de interoperabilidade para contextos externos que referenciam o nome da organização subordinada.
     */
    public String getGrupoParoquialNome() {
        return igrejaNome;
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
                       String tipo,
                       EnderecoLocal endereco,
                       Integer capacidadeMaxima,
                       String descricao,
                       boolean ativo,
                       UUID igrejaId) {
        this.nome = nome;
        this.tipo = tipo;
        this.endereco = endereco != null ? endereco : EnderecoLocal.vazio();
        this.capacidadeMaxima = capacidadeMaxima;
        this.descricao = descricao;
        this.ativo = ativo;
        this.igrejaId = igrejaId;
        this.updatedAt = Instant.now();
    }
}
