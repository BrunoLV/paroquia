package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Representa um tempo/ciclo litúrgico recorrente (ex.: Advento, Quaresma, Tempo Comum),
 * com sua janela anual de vigência (mês/dia de início e fim, repetida todo ano) e um
 * fator de demanda usado para estimar a carga de eventos/celebrações típica desse período.
 * Distingue-se de {@link AnoLiturgicoEntity}, que representa uma instância anual concreta
 * do ciclo dominical (A/B/C), não um tempo litúrgico recorrente.
 */
@Entity
@Table(name = "tb_ciclo_liturgico", schema = "calendario")
public class CicloLiturgicoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    /** Classificação do tempo litúrgico (ex.: Advento, Natal, Quaresma, Páscoa, Tempo Comum). */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ciclo", nullable = false, length = 40)
    private TipoCicloLiturgico tipoCiclo;

    @Column(name = "cor_liturgica", nullable = false, length = 30)
    private String corLiturgica;

    /** Mês (1-12) em que este ciclo tem início dentro do calendário anual. */
    @Column(name = "mes_inicio", nullable = false)
    private Integer mesInicio;

    /** Dia do mês em que este ciclo tem início. */
    @Column(name = "dia_inicio", nullable = false)
    private Integer diaInicio;

    /** Mês (1-12) em que este ciclo termina dentro do calendário anual. */
    @Column(name = "mes_fim", nullable = false)
    private Integer mesFim;

    /** Dia do mês em que este ciclo termina. */
    @Column(name = "dia_fim", nullable = false)
    private Integer diaFim;

    /**
     * Fator multiplicador usado para estimar a demanda/carga de eventos esperada durante
     * este ciclo em relação a um período comum (ex.: valores maiores que 1 indicam tempos
     * de maior movimento pastoral, como Quaresma ou Advento).
     */
    @Column(name = "fator_demanda", nullable = false)
    private Double fatorDemanda;

    @Column(length = 500)
    private String descricao;

    /** Ano litúrgico de referência ({@link AnoLiturgicoEntity#getAno()}) para este ciclo, quando aplicável. */
    @Column(name = "ano_liturgico_ref")
    private Integer anoLiturgicoRef;

    public Integer getAnoLiturgicoRef() {
        return anoLiturgicoRef;
    }

    public void setAnoLiturgicoRef(Integer anoLiturgicoRef) {
        this.anoLiturgicoRef = anoLiturgicoRef;
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

    public TipoCicloLiturgico getTipoCiclo() {
        return tipoCiclo;
    }

    public void setTipoCiclo(TipoCicloLiturgico tipoCiclo) {
        this.tipoCiclo = tipoCiclo;
    }

    public String getCorLiturgica() {
        return corLiturgica;
    }

    public void setCorLiturgica(String corLiturgica) {
        this.corLiturgica = corLiturgica;
    }

    public Integer getMesInicio() {
        return mesInicio;
    }

    public void setMesInicio(Integer mesInicio) {
        this.mesInicio = mesInicio;
    }

    public Integer getDiaInicio() {
        return diaInicio;
    }

    public void setDiaInicio(Integer diaInicio) {
        this.diaInicio = diaInicio;
    }

    public Integer getMesFim() {
        return mesFim;
    }

    public void setMesFim(Integer mesFim) {
        this.mesFim = mesFim;
    }

    public Integer getDiaFim() {
        return diaFim;
    }

    public void setDiaFim(Integer diaFim) {
        this.diaFim = diaFim;
    }

    public Double getFatorDemanda() {
        return fatorDemanda;
    }

    public void setFatorDemanda(Double fatorDemanda) {
        this.fatorDemanda = fatorDemanda;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
