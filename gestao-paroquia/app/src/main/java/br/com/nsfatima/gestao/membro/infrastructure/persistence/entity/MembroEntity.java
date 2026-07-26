package br.com.nsfatima.gestao.membro.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "membros", schema = "calendario")
public class MembroEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(name = "papel", nullable = false, length = 64)
    private String papel;

    @Column(name = "data_entrada")
    private LocalDate dataEntrada;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "igreja_id")
    private UUID igrejaId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public String getPapel() {
        return papel;
    }

    public void setPapel(String papel) {
        this.papel = papel;
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

    public UUID getIgrejaId() {
        return igrejaId;
    }

    public void setIgrejaId(UUID igrejaId) {
        this.igrejaId = igrejaId;
    }
}
