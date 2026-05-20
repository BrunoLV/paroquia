package br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "anos_paroquiais", schema = "calendario")
public class AnoParoquialEntity {

    @Id
    private Integer ano;

    @Column(nullable = false, length = 32)
    private String status;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
