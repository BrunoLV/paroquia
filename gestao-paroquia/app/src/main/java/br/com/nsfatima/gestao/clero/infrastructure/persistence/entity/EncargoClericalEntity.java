package br.com.nsfatima.gestao.clero.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Histórico institucional dos encargos recebidos por um clérigo. */
@Entity
@Table(name = "encargos_clericais", schema = "calendario")
public class EncargoClericalEntity extends BaseAuditableEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID clerigoId;

    @Column(nullable = false)
    private UUID unidadeTerritorialId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuncaoEclesiastica funcao;

    @Column(nullable = false)
    private boolean atual;

    private Instant encerradoEmUtc;

    public UUID getId() {
        return id;
    }

    public void setId(UUID valor) {
        id = valor;
    }

    public UUID getClerigoId() {
        return clerigoId;
    }

    public void setClerigoId(UUID valor) {
        clerigoId = valor;
    }

    public UUID getUnidadeTerritorialId() {
        return unidadeTerritorialId;
    }

    public void setUnidadeTerritorialId(UUID valor) {
        unidadeTerritorialId = valor;
    }

    public FuncaoEclesiastica getFuncao() {
        return funcao;
    }

    public void setFuncao(FuncaoEclesiastica valor) {
        funcao = valor;
    }

    public boolean isAtual() {
        return atual;
    }

    public void setAtual(boolean valor) {
        atual = valor;
    }

    public Instant getEncerradoEmUtc() {
        return encerradoEmUtc;
    }

    public void setEncerradoEmUtc(Instant valor) {
        encerradoEmUtc = valor;
    }
}
