package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.calendario.domain.model.TipoSolenidade;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Catálogo de solenidades e festas litúrgicas (ex.: Natal, Páscoa, Corpus Christi) usadas
 * como referência para o provisionamento anual automático de eventos no calendário
 * paroquial. Cada registro descreve como calcular a data da solenidade em um dado ano —
 * ou por data fixa ({@link #mesFixo}/{@link #diaFixo}) ou por deslocamento em relação à
 * Páscoa ({@link #offsetDiasPascoa}) — e sua prioridade/demanda esperada de agenda.
 */
@Entity
@Table(name = "tb_solenidade_liturgica", schema = "calendario")
public class SolenidadeLiturgicaEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    /** Chave técnica única e estável que identifica a solenidade (independente do nome de exibição). */
    @Column(nullable = false, unique = true, length = 64)
    private String chave;

    @Column(nullable = false, length = 160)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solenidade", nullable = false, length = 32)
    private TipoSolenidade tipoSolenidade;

    /** Mês (1-12) de ocorrência da solenidade quando ela tem data fixa no calendário civil; {@code null} se a data é
     * móvel (baseada na Páscoa). */
    @Column(name = "mes_fixo")
    private Integer mesFixo;

    /** Dia do mês de ocorrência da solenidade quando ela tem data fixa; {@code null} se a data é móvel (baseada na
     * Páscoa). */
    @Column(name = "dia_fixo")
    private Integer diaFixo;

    /** Deslocamento em dias em relação ao Domingo de Páscoa, usado para calcular datas móveis (ex.: Quarta-feira de
     * Cinzas, Corpus Christi); {@code null} se a data é fixa. */
    @Column(name = "offset_dias_pascoa")
    private Integer offsetDiasPascoa;

    @Column(name = "cor_liturgica", nullable = false, length = 32)
    private String corLiturgica;

    /** Fator multiplicador de demanda de agenda esperado para esta solenidade em relação a um dia comum. */
    @Column(name = "fator_demanda", nullable = false)
    private Double fatorDemanda = 1.0;

    /** Nível de prioridade padrão atribuído aos eventos provisionados para esta solenidade na resolução de conflitos de
     * agenda. */
    @Column(name = "nivel_prioridade", nullable = false)
    private Integer nivelPrioridade = 2;

    @Column(length = 500)
    private String descricao;

    /** Indica se a solenidade está ativa para fins de provisionamento automático de eventos; {@code false} desativa a
     * geração sem remover o registro histórico. */
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
