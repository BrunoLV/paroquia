package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Representa o ano litúrgico (não confundir com ano civil): o ciclo anual de leituras
 * e celebrações da Igreja que começa no Advento e determina, entre outras coisas, a
 * letra do ciclo dominical (A, B ou C) usada para escalonar leituras e demanda de eventos.
 * Serve de referência para {@link EventoEntity#getAnoLiturgicoRef()} e para o cálculo do
 * início/fim de tempos litúrgicos usados no planejamento de agenda.
 */
@Entity
@Table(name = "anos_liturgicos", schema = "calendario")
public class AnoLiturgicoEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "ano", nullable = false)
    private Integer ano;

    /** Letra do ciclo dominical de leituras (A, B ou C) vigente neste ano litúrgico. */
    @Column(name = "ciclo_letra", length = 1, nullable = false)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String cicloLetra;

    /** Instante (UTC) em que o Advento — e portanto este ano litúrgico — tem início. */
    @Column(name = "data_inicio_advento", nullable = false)
    private Instant dataInicioAdvento;

    /** Instante (UTC) em que o Tempo Comum encerra, marcando o fim deste ano litúrgico. */
    @Column(name = "data_fim_comum", nullable = false)
    private Instant dataFimComum;

    public AnoLiturgicoEntity() {
    }

    public AnoLiturgicoEntity(
            Integer ano,
            String cicloLetra,
            Instant dataInicioAdvento,
            Instant dataFimComum,
            Instant atualizadoEmUtc) {
        this.ano = ano;
        this.cicloLetra = cicloLetra;
        this.dataInicioAdvento = dataInicioAdvento;
        this.dataFimComum = dataFimComum;
        setAtualizadoEmUtc(atualizadoEmUtc);
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
}
