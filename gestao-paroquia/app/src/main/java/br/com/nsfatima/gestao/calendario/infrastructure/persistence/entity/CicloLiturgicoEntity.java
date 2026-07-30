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

@Entity
@Table(name = "tb_ciclo_liturgico", schema = "calendario")
public class CicloLiturgicoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ciclo", nullable = false, length = 40)
    private TipoCicloLiturgico tipoCiclo;

    @Column(name = "cor_liturgica", nullable = false, length = 30)
    private String corLiturgica;

    @Column(name = "mes_inicio", nullable = false)
    private Integer mesInicio;

    @Column(name = "dia_inicio", nullable = false)
    private Integer diaInicio;

    @Column(name = "mes_fim", nullable = false)
    private Integer mesFim;

    @Column(name = "dia_fim", nullable = false)
    private Integer diaFim;

    @Column(name = "fator_demanda", nullable = false)
    private Double fatorDemanda;

    @Column(length = 500)
    private String descricao;

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
