package br.com.nsfatima.gestao.local.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Mapeamento JPA do agregado de domínio {@code Local} na tabela {@code calendario.locais}.
 *
 * <p>Invariantes de persistência:
 * <ul>
 *   <li>{@code nome} é obrigatório e único;</li>
 *   <li>{@code tipo} é obrigatório;</li>
 *   <li>{@code organizacaoId} vincula o local a uma igreja (Matriz ou Comunidade);</li>
 *   <li>{@code deletedAt} implementa o soft delete exigido pelo negócio;</li>
 *   <li>Endereço estruturado (CEP, logradouro, número, complemento, bairro, cidade, UF) é persistido
 *       em colunas dedicadas para busca e exibição padronizada.</li>
 * </ul>
 */
@Entity
@Table(name = "locais", schema = "calendario")
public class LocalEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160, unique = true)
    private String nome;

    @Column(nullable = false, length = 32)
    private String tipo;

    @Column(length = 500)
    private String endereco;

    @Column(length = 9)
    private String cep;

    @Column(length = 150)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 100)
    private String bairro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String uf;

    private Integer capacidade;

    @Column(name = "capacidade_maxima")
    private Integer capacidadeMaxima;

    @Column(columnDefinition = "TEXT")
    private String caracteristicas;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "organizacao_id")
    private UUID organizacaoId;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    public void onPrePersist() {
        syncLegacyFields();
    }

    @PreUpdate
    public void onPreUpdate() {
        syncLegacyFields();
    }

    private void syncLegacyFields() {
        if (this.capacidadeMaxima != null && this.capacidade == null) {
            this.capacidade = this.capacidadeMaxima;
        } else if (this.capacidade != null && this.capacidadeMaxima == null) {
            this.capacidadeMaxima = this.capacidade;
        }
        if (this.descricao != null && this.caracteristicas == null) {
            this.caracteristicas = this.descricao;
        } else if (this.caracteristicas != null && this.descricao == null) {
            this.descricao = this.caracteristicas;
        }
        if (this.logradouro != null && (this.endereco == null || this.endereco.isBlank())) {
            this.endereco = buildFormattedAddress();
        }
    }

    private String buildFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (logradouro != null) {
            sb.append(logradouro);
        }
        if (numero != null && !numero.isBlank()) {
            sb.append(", ").append(numero);
        }
        if (complemento != null && !complemento.isBlank()) {
            sb.append(" - ").append(complemento);
        }
        if (bairro != null && !bairro.isBlank()) {
            sb.append(", ").append(bairro);
        }
        if (cidade != null && !cidade.isBlank()) {
            sb.append(" - ").append(cidade);
        }
        if (uf != null && !uf.isBlank()) {
            sb.append("/").append(uf);
        }
        if (cep != null && !cep.isBlank()) {
            sb.append(" (CEP: ").append(cep).append(")");
        }
        return sb.toString();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
        this.capacidadeMaxima = capacidade;
    }

    public Integer getCapacidadeMaxima() {
        return capacidadeMaxima != null ? capacidadeMaxima : capacidade;
    }

    public void setCapacidadeMaxima(Integer capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
        this.capacidade = capacidadeMaxima;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(String caracteristicas) {
        this.caracteristicas = caracteristicas;
        this.descricao = caracteristicas;
    }

    public String getDescricao() {
        return descricao != null ? descricao : caracteristicas;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
        this.caracteristicas = descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    /**
     * Reidrata os instantes de auditoria já persistidos ao remontar a entidade a partir do domínio, para que
     * o {@code merge} não anule em memória o instante de criação. O listener de auditoria continua sendo a
     * fonte dos valores gravados no banco.
     *
     * @param criadoEm instante de criação já persistido
     * @param atualizadoEm instante da última atualização já persistido
     */
    public void restaurarAuditoria(Instant criadoEm, Instant atualizadoEm) {
        setCriadoEmUtc(criadoEm);
        setAtualizadoEmUtc(atualizadoEm);
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
