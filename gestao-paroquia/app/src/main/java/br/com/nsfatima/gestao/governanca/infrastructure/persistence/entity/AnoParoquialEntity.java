package br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "anos_paroquiais", schema = "calendario")
public class AnoParoquialEntity {

    @Id
    private Integer ano;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AnoParoquialStatus status;

    @Column(name = "data_fechamento_utc")
    private Instant dataFechamentoUtc;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public AnoParoquialStatus getStatus() {
        return status;
    }

    public void setStatus(AnoParoquialStatus status) {
        this.status = status;
    }

    public Instant getDataFechamentoUtc() {
        return dataFechamentoUtc;
    }

    public void setDataFechamentoUtc(Instant dataFechamentoUtc) {
        this.dataFechamentoUtc = dataFechamentoUtc;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
