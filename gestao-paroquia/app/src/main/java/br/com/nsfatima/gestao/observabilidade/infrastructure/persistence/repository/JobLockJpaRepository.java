package br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.JobLockEntity;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface JobLockJpaRepository extends JpaRepository<JobLockEntity, String> {

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE calendario.job_locks
            SET locked_until = :until, locked_at = :now
            WHERE name = :name AND locked_until <= :now
            """, nativeQuery = true)
    int updateLock(@Param("name") String name, @Param("until") Instant until, @Param("now") Instant now);

    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO calendario.job_locks (name, locked_until, locked_at)
            VALUES (:name, :until, :now)
            """, nativeQuery = true)
    void insertLock(@Param("name") String name, @Param("until") Instant until, @Param("now") Instant now);

    @Transactional
    default boolean acquireLock(String name, Instant until, Instant now) {
        if (updateLock(name, until, now) > 0) {
            return true;
        }
        try {
            insertLock(name, until, now);
            return true;
        } catch (Exception e) {
            // Already exists and was not updated (still locked)
            return false;
        }
    }

    @Transactional
    @Modifying
    @Query(value = "UPDATE calendario.job_locks SET locked_until = :now WHERE name = :name", nativeQuery = true)
    void releaseLock(@Param("name") String name, @Param("now") Instant now);

    default void releaseLock(String name) {
        releaseLock(name, Instant.now());
    }
}
