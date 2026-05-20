package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.JobLockEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.JobLockJpaRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class JobLockJpaRepositoryTest {

    @Autowired
    private JobLockJpaRepository repository;

    @Test
    void shouldAcquireAndReleaseLock() {
        String lockName = "YEARLY_RECURRENCE_JOB";
        Instant now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);

        // 1. Try to acquire
        boolean acquired = repository.acquireLock(lockName, now.plusSeconds(60), now);
        assertTrue(acquired, "Should acquire free lock");

        // 2. Try to acquire again (should fail)
        boolean acquiredAgain = repository.acquireLock(lockName, now.plusSeconds(120), now);
        assertFalse(acquiredAgain, "Should not acquire already held lock");

        // 3. Release
        repository.releaseLock(lockName, now);

        // 4. Acquire again
        boolean acquiredThird = repository.acquireLock(lockName, now.plusSeconds(180), now);
        assertTrue(acquiredThird, "Should acquire released lock");
    }
}
