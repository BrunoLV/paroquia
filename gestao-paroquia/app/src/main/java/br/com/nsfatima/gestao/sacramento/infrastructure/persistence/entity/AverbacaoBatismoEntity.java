package br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.sacramento.domain.model.StatusAverbacao;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoEventoAverbacao;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Entidade JPA de {@code calendario.averbacoes_batismo}: nota marginal append-only (sem DELETE). */
@Entity
@Table(name = "averbacoes_batismo", schema = "calendario")
public class AverbacaoBatismoEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assento_id", nullable = false)
    private AssentoSacramentalEntity assento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 32)
    private TipoEventoAverbacao tipoEvento;

    @Column(name = "data_averbacao", nullable = false)
    private LocalDate dataAverbacao;

    @Column(name = "numero_documento", length = 128)
    private String numeroDocumento;

    @Column(name = "teor_nota", nullable = false, columnDefinition = "TEXT")
    private String teorNota;

    @Column(name = "reservada", nullable = false)
    private boolean reservada;

    @Column(name = "inserida_por", length = 128)
    private String inseridaPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private StatusAverbacao status;

    @Column(name = "homologada_por", length = 128)
    private String homologadaPor;

    @Column(name = "homologada_em_utc")
    private Instant homologadaEmUtc;

    public AverbacaoBatismoEntity() {
    }

    public AverbacaoBatismoEntity(UUID id, AssentoSacramentalEntity assento) {
        this.id = id;
        this.assento = assento;
    }

    public UUID getId() {
        return id;
    }

    public TipoEventoAverbacao getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEventoAverbacao tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public LocalDate getDataAverbacao() {
        return dataAverbacao;
    }

    public void setDataAverbacao(LocalDate dataAverbacao) {
        this.dataAverbacao = dataAverbacao;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTeorNota() {
        return teorNota;
    }

    public void setTeorNota(String teorNota) {
        this.teorNota = teorNota;
    }

    public boolean isReservada() {
        return reservada;
    }

    public void setReservada(boolean reservada) {
        this.reservada = reservada;
    }

    public String getInseridaPor() {
        return inseridaPor;
    }

    public void setInseridaPor(String inseridaPor) {
        this.inseridaPor = inseridaPor;
    }

    public StatusAverbacao getStatus() {
        return status;
    }

    public void setStatus(StatusAverbacao status) {
        this.status = status;
    }

    public String getHomologadaPor() {
        return homologadaPor;
    }

    public void setHomologadaPor(String homologadaPor) {
        this.homologadaPor = homologadaPor;
    }

    public Instant getHomologadaEmUtc() {
        return homologadaEmUtc;
    }

    public void setHomologadaEmUtc(Instant homologadaEmUtc) {
        this.homologadaEmUtc = homologadaEmUtc;
    }
}
