package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;

/**
 * Entidade relacional JPA da tabela {@code calendario.ativos_patrimoniais}. Bem patrimonial móvel
 * pertencente de forma fixa a uma unidade territorial (MATRIZ ou COMUNIDADE), identificado por um
 * código de tombamento anual único. Após o cadastro apenas {@code descricao} é mutável; o descarte
 * é exclusão lógica ({@code deletedAt}).
 */
@Entity
@Table(name = "ativos_patrimoniais", schema = "calendario")
public class AtivoPatrimonialEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "codigo_tombamento", nullable = false, unique = true, length = 32)
    private String codigoTombamento;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 32)
    private AtivoCategoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AtivoStatus status;

    @Column(name = "is_reservavel", nullable = false)
    private boolean reservavel;

    @Column(name = "grupo_paroquial_id", nullable = false)
    private UUID grupoParoquialId;

    @Column(name = "data_aquisicao", nullable = false)
    private LocalDate dataAquisicao;

    @Column(name = "valor_aquisicao", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorAquisicao;

    @Column(name = "cadastrado_por_id")
    private UUID cadastradoPorId;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public AtivoPatrimonialEntity() {
    }

    public AtivoPatrimonialEntity(
            UUID id,
            String nome,
            String codigoTombamento,
            String descricao,
            AtivoCategoria categoria,
            AtivoStatus status,
            boolean reservavel,
            UUID grupoParoquialId,
            LocalDate dataAquisicao,
            BigDecimal valorAquisicao,
            UUID cadastradoPorId,
            Instant criadoEmUtc,
            Instant updatedAt,
            Instant deletedAt,
            long version
    ) {
        this.id = id;
        this.nome = nome;
        this.codigoTombamento = codigoTombamento;
        this.descricao = descricao;
        this.categoria = categoria;
        this.status = status;
        this.reservavel = reservavel;
        this.grupoParoquialId = grupoParoquialId;
        this.dataAquisicao = dataAquisicao;
        this.valorAquisicao = valorAquisicao;
        this.cadastradoPorId = cadastradoPorId;
        this.criadoEmUtc = criadoEmUtc;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
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

    public String getCodigoTombamento() {
        return codigoTombamento;
    }
    public void setCodigoTombamento(String codigoTombamento) {
        this.codigoTombamento = codigoTombamento;
    }

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public AtivoCategoria getCategoria() {
        return categoria;
    }
    public void setCategoria(AtivoCategoria categoria) {
        this.categoria = categoria;
    }

    public AtivoStatus getStatus() {
        return status;
    }
    public void setStatus(AtivoStatus status) {
        this.status = status;
    }

    public boolean isReservavel() {
        return reservavel;
    }
    public void setReservavel(boolean reservavel) {
        this.reservavel = reservavel;
    }

    public UUID getGrupoParoquialId() {
        return grupoParoquialId;
    }
    public void setGrupoParoquialId(UUID grupoParoquialId) {
        this.grupoParoquialId = grupoParoquialId;
    }

    public LocalDate getDataAquisicao() {
        return dataAquisicao;
    }
    public void setDataAquisicao(LocalDate dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }

    public BigDecimal getValorAquisicao() {
        return valorAquisicao;
    }
    public void setValorAquisicao(BigDecimal valorAquisicao) {
        this.valorAquisicao = valorAquisicao;
    }

    public UUID getCadastradoPorId() {
        return cadastradoPorId;
    }
    public void setCadastradoPorId(UUID cadastradoPorId) {
        this.cadastradoPorId = cadastradoPorId;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }
    public void setCriadoEmUtc(Instant criadoEmUtc) {
        this.criadoEmUtc = criadoEmUtc;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }
    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public long getVersion() {
        return version;
    }
    public void setVersion(long version) {
        this.version = version;
    }
}
