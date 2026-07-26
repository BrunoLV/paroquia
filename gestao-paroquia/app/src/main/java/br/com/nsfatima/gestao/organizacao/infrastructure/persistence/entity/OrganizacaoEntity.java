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
@Table(name = "organizacoes", schema = "calendario")
public class OrganizacaoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 255, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TipoOrganizacao tipo;

    @Column(length = 255)
    private String contato;

    @Column(name = "coordenador", length = 255)
    private String coordenador;

    @Column(name = "vice_coordenador", length = 255)
    private String viceCoordenador;

    @Column(nullable = false)
    private boolean ativo = true;
    @Column(name = "parent_id")
    private UUID parentId;
    @Column(name = "pastoral_oficial_id")
    private UUID pastoralOficialId;

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

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public String getCoordenador() {
        return coordenador;
    }

    public void setCoordenador(String coordenador) {
        this.coordenador = coordenador;
    }

    public String getViceCoordenador() {
        return viceCoordenador;
    }

    public void setViceCoordenador(String viceCoordenador) {
        this.viceCoordenador = viceCoordenador;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public UUID getPastoralOficialId() {
        return pastoralOficialId;
    }

    public void setPastoralOficialId(UUID pastoralOficialId) {
        this.pastoralOficialId = pastoralOficialId;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
