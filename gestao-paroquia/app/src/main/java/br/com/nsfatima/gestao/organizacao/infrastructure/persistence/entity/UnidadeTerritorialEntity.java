package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entidade JPA que persiste um nó da hierarquia territorial da paróquia — o espelho de armazenamento de
 * {@code UnidadeTerritorial}. {@code parentId} referencia a unidade-mãe (auto-relacionamento por ID, sem FK
 * JPA navegável, mesma abordagem de {@code GrupoParoquialEntity}).
 */
@Entity
@Table(name = "unidade_territorial", schema = "calendario")
public class UnidadeTerritorialEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TipoUnidadeTerritorial tipo;

    @Column
    private String contato;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "parent_id")
    private UUID parentId;

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

    public TipoUnidadeTerritorial getTipo() {
        return tipo;
    }

    public void setTipo(TipoUnidadeTerritorial tipo) {
        this.tipo = tipo;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }
}
