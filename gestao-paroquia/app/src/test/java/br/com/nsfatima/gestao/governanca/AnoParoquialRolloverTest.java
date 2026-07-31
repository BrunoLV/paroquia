package br.com.nsfatima.gestao.governanca;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.domain.service.AnoParoquialRolloverService;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@Transactional
class AnoParoquialRolloverTest {

    @Autowired
    private AnoParoquialJpaRepository repository;

    @Autowired
    private AnoParoquialRolloverService rolloverService;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void shouldHaveScheduledCronExpressionForDec31EndOfDay() throws NoSuchMethodException {
        Method method = AnoParoquialRolloverService.class.getMethod("rolloverYearAutomatically");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("59 59 23 31 12 *");
    }

    @Test
    void shouldRolloverYearSettingItToConcluidoAndInitializingNextYear() {
        // Arrange: Year 2026 is EM_PLANEJAMENTO
        AnoParoquialEntity entity = new AnoParoquialEntity();
        entity.setAno(2026);
        entity.setStatus(AnoParoquialStatus.EM_PLANEJAMENTO.name());
        repository.saveAndFlush(entity);

        // Act: conclude Year 2026
        rolloverService.concludeYear(2026);

        // Assert: 2026 is CONCLUIDO
        Optional<AnoParoquialEntity> year2026 = repository.findByAno(2026);
        assertThat(year2026).isPresent();
        assertThat(year2026.get().getStatus()).isEqualTo(AnoParoquialStatus.CONCLUIDO.name());

        // Assert: 2027 is created as EM_PLANEJAMENTO
        Optional<AnoParoquialEntity> year2027 = repository.findByAno(2027);
        assertThat(year2027).isPresent();
        assertThat(year2027.get().getStatus()).isEqualTo(AnoParoquialStatus.EM_PLANEJAMENTO.name());
    }
}
