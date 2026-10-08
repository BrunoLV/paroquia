package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.organizacao.domain.model.AmbitoOrganismo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade JPA que persiste um item do catálogo oficial de Pastorais e Movimentos — o espelho de
 * armazenamento de {@code PastoralMovimentoOficial}. O nome é único no banco para impedir cadastros
 * duplicados do mesmo item oficial sob nomes diferentes.
 */
@Entity
@Table(name = "pastorais_movimentos_oficiais", schema = "calendario")
public class PastoralMovimentoOficialEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoOrganizacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private AmbitoOrganismo ambito;

    @Column(name = "estatuto_autoridade", length = 150)
    private String estatutoAutoridade;

    @Column(name = "estatuto_data")
    private LocalDate estatutoData;

    @Column(name = "estatuto_referencia", length = 255)
    private String estatutoReferencia;

    public AmbitoOrganismo getAmbito() {
        return ambito;
    }

    public void setAmbito(AmbitoOrganismo ambito) {
        this.ambito = ambito;
    }

    public String getEstatutoAutoridade() {
        return estatutoAutoridade;
    }

    public void setEstatutoAutoridade(String estatutoAutoridade) {
        this.estatutoAutoridade = estatutoAutoridade;
    }

    public LocalDate getEstatutoData() {
        return estatutoData;
    }

    public void setEstatutoData(LocalDate estatutoData) {
        this.estatutoData = estatutoData;
    }

    public String getEstatutoReferencia() {
        return estatutoReferencia;
    }

    public void setEstatutoReferencia(String estatutoReferencia) {
        this.estatutoReferencia = estatutoReferencia;
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

    public TipoOrganizacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoOrganizacao tipo) {
        this.tipo = tipo;
    }
}
