package br.com.nsfatima.gestao.iam.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Atribuição de um papel de acesso irrestrito ({@code PapelGlobal}) a um {@code Fiel}, sem vínculo a
 * nenhuma organização — diferente de {@code MembroGrupoEntity}, que sempre exige {@code grupoParoquialId}.
 */
@Entity
@Table(name = "papel_global", schema = "calendario")
public class PapelGlobalEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(name = "fiel_id", nullable = false)
    private UUID fielId;

    @Column(nullable = false, length = 32)
    private String papel;

    @Column(nullable = false)
    private boolean ativo = true;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getFielId() {
        return fielId;
    }

    public void setFielId(UUID fielId) {
        this.fielId = fielId;
    }

    public String getPapel() {
        return papel;
    }

    public void setPapel(String papel) {
        this.papel = papel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
