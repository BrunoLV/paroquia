package br.com.nsfatima.gestao.governanca;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.domain.service.AnoParoquialSeeder;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
class AnoParoquialSeedingTest {

    @Autowired
    private AnoParoquialJpaRepository repository;

    @Autowired
    private AnoParoquialSeeder seeder;

    @Test
    @Transactional
    void shouldSeedCurrentYearWhenDatabaseIsEmpty() {
        // Arrange
        repository.deleteAll();
        assertThat(repository.count()).isZero();

        // Act
        seeder.seedIfEmpty();

        // Assert
        int currentYear = LocalDate.now().getYear();
        Optional<AnoParoquialEntity> seeded = repository.findByAno(currentYear);
        assertThat(seeded).isPresent();
        assertThat(seeded.get().getStatus()).isEqualTo(AnoParoquialStatus.EM_PLANEJAMENTO.name());
    }
}
