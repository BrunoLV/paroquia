package br.com.nsfatima.gestao.membro.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
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
 * Vínculo de um {@code Fiel} (pessoa física) a uma organização, com o papel que exerce ali. Substitui as três
 * tabelas legadas ({@code membros}, {@code membros_grupoParoquial}, {@code membros_organizacoes}) unificadas pela
 * track {@code membro_grupo_por_fiel}: participação pastoral não exige mais que o Fiel possua um {@code Usuario}
 * (login) — ver {@code AdicionarMembroUseCase}, que nunca toca {@code UsuarioEntity}. Um mesmo Fiel pode ter
 * múltiplas linhas (uma por organização/papel); múltiplas participações são simplesmente múltiplas linhas.
 */
@Entity
@Table(name = "membro_grupo", schema = "calendario")
public class MembroGrupoEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(name = "fiel_id", nullable = false)
    private UUID fielId;

    @Column(name = "grupo_paroquial_id", nullable = false)
    private UUID grupoParoquialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_grupo", nullable = false, length = 32)
    private TipoGrupo tipoGrupo;

    @Column(nullable = false, length = 64)
    private String papel;

    @Column(name = "igreja_vinculo_id")
    private UUID igrejaVinculoId;

    @Column(name = "data_entrada")
    private LocalDate dataEntrada;

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

    public UUID getGrupoParoquialId() {
        return grupoParoquialId;
    }

    public void setGrupoParoquialId(UUID grupoParoquialId) {
        this.grupoParoquialId = grupoParoquialId;
    }

    public TipoGrupo getTipoGrupo() {
        return tipoGrupo;
    }

    public void setTipoGrupo(TipoGrupo tipoGrupo) {
        this.tipoGrupo = tipoGrupo;
    }

    public String getPapel() {
        return papel;
    }

    public void setPapel(String papel) {
        this.papel = papel;
    }

    public UUID getIgrejaVinculoId() {
        return igrejaVinculoId;
    }

    public void setIgrejaVinculoId(UUID igrejaVinculoId) {
        this.igrejaVinculoId = igrejaVinculoId;
    }

    public LocalDate getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDate dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
