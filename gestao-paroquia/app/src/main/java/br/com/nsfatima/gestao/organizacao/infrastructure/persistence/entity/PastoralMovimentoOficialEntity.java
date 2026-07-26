package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "pastorais_movimentos_oficiais", schema = "calendario")
public class PastoralMovimentoOficialEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 150, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoOrganizacao tipo;

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
