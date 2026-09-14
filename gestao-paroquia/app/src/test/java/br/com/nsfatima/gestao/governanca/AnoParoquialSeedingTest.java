package br.com.nsfatima.gestao.governanca;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.application.service.AnoParoquialSeeder;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
class AnoParoquialSeedingTest {

    @Autowired
    private AnoParoquialJpaRepository repository;

    @Autowired
    private AnoParoquialSeeder seeder;

    @Test
    @Transactional
    void deveSemearAnoAtualQuandoBancoEstiverVazio() {
        // Arrange
        repository.deleteAll();
        assertThat(repository.count()).isZero();

        // Act
        seeder.semearSeVazio();

        // Assert
        int anoCorrente = LocalDate.now().getYear();
        Optional<AnoParoquialEntity> seeded = repository.findByAno(anoCorrente);
        assertThat(seeded).isPresent();
        assertThat(seeded.get().getStatus()).isEqualTo(AnoParoquialStatus.EM_PLANEJAMENTO);
    }
}
