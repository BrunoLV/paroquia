package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Contador do último número de tombamento emitido para um ano-calendário, usado por
 * {@link br.com.nsfatima.gestao.patrimonio.application.service.GeradorNumeroTombamento} para gerar
 * números sequenciais únicos por ano.
 */
@Entity
@Table(name = "patrimonio_numeracao_anual", schema = "calendario")
public class PatrimonioNumeracaoAnualEntity extends BaseAuditableEntity {

    @Id
    private Integer ano;

    @Column(name = "ultimo_numero", nullable = false)
    private Integer ultimoNumero = 0;

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getUltimoNumero() {
        return ultimoNumero;
    }

    public void setUltimoNumero(Integer ultimoNumero) {
        this.ultimoNumero = ultimoNumero;
    }
}
