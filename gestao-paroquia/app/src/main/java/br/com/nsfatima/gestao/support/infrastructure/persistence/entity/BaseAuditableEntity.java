package br.com.nsfatima.gestao.support.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Superclasse mapeada padronizada para auditoria universal de entidades JPA.
 *
 * <p>Centraliza o rastreamento temporal e autoral via {@link AuditingEntityListener} do Spring Data JPA,
 * garantindo o preenchimento automatico e consistente das colunas {@code criado_em_utc},
 * {@code atualizado_em_utc}, {@code criado_por} e {@code atualizado_por} em todas as entidades persistidas.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditableEntity extends BaseVersionedEntity {

    @CreatedDate
    @Column(name = "criado_em_utc", nullable = false, updatable = false)
    private Instant criadoEmUtc;

    @LastModifiedDate
    @Column(name = "atualizado_em_utc", nullable = false)
    private Instant atualizadoEmUtc;

    @CreatedBy
    @Column(name = "criado_por", nullable = false, updatable = false, length = 128)
    private String criadoPor;

    @LastModifiedBy
    @Column(name = "atualizado_por", nullable = false, length = 128)
    private String atualizadoPor;

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public void setCriadoEmUtc(Instant criadoEmUtc) {
        this.criadoEmUtc = criadoEmUtc;
    }

    public Instant getAtualizadoEmUtc() {
        return atualizadoEmUtc;
    }

    public void setAtualizadoEmUtc(Instant atualizadoEmUtc) {
        this.atualizadoEmUtc = atualizadoEmUtc;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(String criadoPor) {
        this.criadoPor = criadoPor;
    }

    public String getAtualizadoPor() {
        return atualizadoPor;
    }

    public void setAtualizadoPor(String atualizadoPor) {
        this.atualizadoPor = atualizadoPor;
    }

    public Instant getCreatedAt() {
        return criadoEmUtc;
    }

    public void setCreatedAt(Instant createdAt) {
        this.criadoEmUtc = createdAt;
    }

    public Instant getUpdatedAt() {
        return atualizadoEmUtc;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.atualizadoEmUtc = updatedAt;
    }
}

