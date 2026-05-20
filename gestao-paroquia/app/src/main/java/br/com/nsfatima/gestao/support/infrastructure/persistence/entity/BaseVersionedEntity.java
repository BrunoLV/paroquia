package br.com.nsfatima.gestao.support.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public abstract class BaseVersionedEntity {

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public Long getVersion() {
        return version;
    }
}
