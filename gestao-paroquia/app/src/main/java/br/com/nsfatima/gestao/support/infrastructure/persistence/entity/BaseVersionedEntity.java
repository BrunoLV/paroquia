package br.com.nsfatima.gestao.support.infrastructure.persistence.entity;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class BaseVersionedEntity {

    public Long getVersion() {
        return 0L;
    }
}
