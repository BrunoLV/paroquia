package br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Lock distribuído baseado em banco de dados, usado para coordenar a execução de jobs
 * agendados (ex.: rotinas de métricas/observabilidade) entre múltiplas instâncias da
 * aplicação, evitando execução concorrente do mesmo job. {@code name} identifica o job de
 * forma única; {@code lockedUntil} define até quando o lock é considerado válido (permitindo
 * que outra instância o retome caso a anterior trave/caia sem liberar explicitamente); ver
 * {@code JobLockJpaRepository#acquireLock} para a semântica de aquisição.
 */
@Entity
@Table(name = "job_locks", schema = "calendario")
public class JobLockEntity extends BaseAuditableEntity {

    @Id
    @Column(length = 128)
    private String name;

    @Column(name = "locked_until", nullable = false)
    private Instant lockedUntil;

    @Column(name = "locked_at", nullable = false)
    private Instant lockedAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public Instant getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Instant lockedAt) {
        this.lockedAt = lockedAt;
    }
}
