package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "anos_liturgicos", schema = "calendario")
public class AnoLiturgicoEntity {

    @Id
    @Column(name = "ano", nullable = false)
    private Integer ano;

    @Column(name = "ciclo_letra", length = 1, nullable = false, columnDefinition = "CHAR(1)")
    private String cicloLetra;

    @Column(name = "data_inicio_advento", nullable = false)
    private Instant dataInicioAdvento;

    @Column(name = "data_fim_comum", nullable = false)
    private Instant dataFimComum;

    @Column(name = "atualizado_em_utc", nullable = false)
    private Instant atualizadoEmUtc;

    public AnoLiturgicoEntity() {
    }

    public AnoLiturgicoEntity(Integer ano, String cicloLetra,
                              Instant dataInicioAdvento, Instant dataFimComum, Instant atualizadoEmUtc) {
        this.ano = ano;
        this.cicloLetra = cicloLetra;
        this.dataInicioAdvento = dataInicioAdvento;
        this.dataFimComum = dataFimComum;
        this.atualizadoEmUtc = atualizadoEmUtc;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getCicloLetra() {
        return cicloLetra;
    }

    public void setCicloLetra(String cicloLetra) {
        this.cicloLetra = cicloLetra;
    }

    public Instant getDataInicioAdvento() {
        return dataInicioAdvento;
    }

    public void setDataInicioAdvento(Instant dataInicioAdvento) {
        this.dataInicioAdvento = dataInicioAdvento;
    }

    public Instant getDataFimComum() {
        return dataFimComum;
    }

    public void setDataFimComum(Instant dataFimComum) {
        this.dataFimComum = dataFimComum;
    }

    public Instant getAtualizadoEmUtc() {
        return atualizadoEmUtc;
    }

    public void setAtualizadoEmUtc(Instant atualizadoEmUtc) {
        this.atualizadoEmUtc = atualizadoEmUtc;
    }
}
