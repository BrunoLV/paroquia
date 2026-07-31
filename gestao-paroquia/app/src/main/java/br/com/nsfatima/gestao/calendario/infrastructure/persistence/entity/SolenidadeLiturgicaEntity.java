package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.calendario.domain.model.TipoSolenidade;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "tb_solenidade_liturgica", schema = "calendario")
public class SolenidadeLiturgicaEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String chave;

    @Column(nullable = false, length = 160)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solenidade", nullable = false, length = 32)
    private TipoSolenidade tipoSolenidade;

    @Column(name = "mes_fixo")
    private Integer mesFixo;

    @Column(name = "dia_fixo")
    private Integer diaFixo;

    @Column(name = "offset_dias_pascoa")
    private Integer offsetDiasPascoa;

    @Column(name = "cor_liturgica", nullable = false, length = 32)
    private String corLiturgica;

    @Column(name = "fator_demanda", nullable = false)
    private Double fatorDemanda = 1.0;

    @Column(name = "nivel_prioridade", nullable = false)
    private Integer nivelPrioridade = 2;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private boolean ativo = true;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoSolenidade getTipoSolenidade() {
        return tipoSolenidade;
    }

    public void setTipoSolenidade(TipoSolenidade tipoSolenidade) {
        this.tipoSolenidade = tipoSolenidade;
    }

    public Integer getMesFixo() {
        return mesFixo;
    }

    public void setMesFixo(Integer mesFixo) {
        this.mesFixo = mesFixo;
    }

    public Integer getDiaFixo() {
        return diaFixo;
    }

    public void setDiaFixo(Integer diaFixo) {
        this.diaFixo = diaFixo;
    }

    public Integer getOffsetDiasPascoa() {
        return offsetDiasPascoa;
    }

    public void setOffsetDiasPascoa(Integer offsetDiasPascoa) {
        this.offsetDiasPascoa = offsetDiasPascoa;
    }

    public String getCorLiturgica() {
        return corLiturgica;
    }

    public void setCorLiturgica(String corLiturgica) {
        this.corLiturgica = corLiturgica;
    }

    public Double getFatorDemanda() {
        return fatorDemanda;
    }

    public void setFatorDemanda(Double fatorDemanda) {
        this.fatorDemanda = fatorDemanda;
    }

    public Integer getNivelPrioridade() {
        return nivelPrioridade;
    }

    public void setNivelPrioridade(Integer nivelPrioridade) {
        this.nivelPrioridade = nivelPrioridade;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
