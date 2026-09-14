package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entidade JPA que persiste um nó da hierarquia organizacional da paróquia — o espelho de armazenamento de
 * {@code GrupoParoquial}. {@code parentId} referencia a organização-mãe (auto-relacionamento por ID, sem FK JPA
 * navegável, para manter os contextos independentes) e {@code pastoralOficialId} vincula o registro ao
 * catálogo oficial de Pastorais/Movimentos quando o {@code tipo} for {@code PASTORAL} ou {@code MOVIMENTO}.
 */
@Entity
@Table(name = "grupo_paroquial", schema = "calendario")
public class GrupoParoquialEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TipoGrupo tipo;

    @Column()
    private String contato;

    @Column(name = "coordenador")
    private String coordenador;

    @Column(name = "vice_coordenador")
    private String viceCoordenador;

    @Column(nullable = false)
    private boolean ativo = true;
    @Column(name = "parent_id")
    private UUID parentId;
    @Column(name = "pastoral_oficial_id")
    private UUID pastoralOficialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "setor_pastoral", length = 64)
    private SetorPastoral setorPastoral;

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

    public TipoGrupo getTipo() {
        return tipo;
    }

    public void setTipo(TipoGrupo tipo) {
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

    public SetorPastoral getSetorPastoral() {
        return setorPastoral;
    }

    public void setSetorPastoral(SetorPastoral setorPastoral) {
        this.setorPastoral = setorPastoral;
    }
}

