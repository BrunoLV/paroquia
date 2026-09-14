package br.com.nsfatima.gestao.governanca;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.application.service.AnoParoquialRolloverService;
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
    void deveTerScheduledCronExpressionParaDec31FimDeDia() throws NoSuchMethodException {
        Method method = AnoParoquialRolloverService.class.getMethod("transicionarAnoAutomaticamente");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("59 59 23 31 12 *");
    }

    @Test
    void deveVirarAnoDefinindoComoConcluidoEInicializandoProximoAno() {
        // Arrange: ano 2026 está EM_PLANEJAMENTO
        AnoParoquialEntity entity = new AnoParoquialEntity();
        entity.setAno(2026);
        entity.setStatus(AnoParoquialStatus.EM_PLANEJAMENTO);
        repository.saveAndFlush(entity);

        // Act: conclui o ano 2026
        rolloverService.concluirAno(2026);

        // Assert: 2026 está CONCLUIDO
        Optional<AnoParoquialEntity> ano2026 = repository.findByAno(2026);
        assertThat(ano2026).isPresent();
        assertThat(ano2026.get().getStatus()).isEqualTo(AnoParoquialStatus.CONCLUIDO);

        // Assert: 2027 é criado como EM_PLANEJAMENTO
        Optional<AnoParoquialEntity> ano2027 = repository.findByAno(2027);
        assertThat(ano2027).isPresent();
        assertThat(ano2027.get().getStatus()).isEqualTo(AnoParoquialStatus.EM_PLANEJAMENTO);
    }
}
