package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;

@Entity
@Table(name = "ativos_patrimoniais", schema = "calendario")
public class AtivoPatrimonialEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(name = "numero_tombamento", nullable = false, unique = true, length = 32)
    private String numeroTombamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AtivoCategoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AtivoStatus status = AtivoStatus.DISPONIVEL;

    @Column(nullable = false)
    private boolean reservavel;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(name = "data_aquisicao")
    private LocalDate dataAquisicao;

    @Column(name = "valor_estimado", precision = 12, scale = 2)
    private BigDecimal valorEstimado;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

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

    public String getNumeroTombamento() {
        return numeroTombamento;
    }

    public void setNumeroTombamento(String numeroTombamento) {
        this.numeroTombamento = numeroTombamento;
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

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public LocalDate getDataAquisicao() {
        return dataAquisicao;
    }

    public void setDataAquisicao(LocalDate dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }

    public BigDecimal getValorEstimado() {
        return valorEstimado;
    }

    public void setValorEstimado(BigDecimal valorEstimado) {
        this.valorEstimado = valorEstimado;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public void setCriadoEmUtc(Instant criadoEmUtc) {
        this.criadoEmUtc = criadoEmUtc;
    }
}
