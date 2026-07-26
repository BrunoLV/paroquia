package br.com.nsfatima.gestao.governanca.domain.service;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AnoParoquialRolloverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnoParoquialRolloverService.class);
    private final AnoParoquialJpaRepository repository;

    public AnoParoquialRolloverService(AnoParoquialJpaRepository repository) {
        this.repository = repository;
    }

    public Optional<AnoParoquialEntity> get(Integer ano) {
        return repository.findByAno(ano);
    }

    /**
     * Scheduled task to rollover the parish year.
     * Runs automatically on December 31st at 23:59:59.
     */
    @Scheduled(cron = "59 59 23 31 12 *")
    @Transactional
    public void rolloverYearAutomatically() {
        int currentYear = LocalDate.now().getYear();
        LOGGER.info("Starting automated Parish Year rollover for year {}", currentYear);
        concludeYear(currentYear);
    }

    /**
     * Concludes a given year and automatically creates the next one in EM_PLANEJAMENTO if not exists.
     */
    @Transactional
    public void concludeYear(int year) {
        AnoParoquialEntity entity = repository.findByAno(year)
                .orElseGet(() -> {
                    AnoParoquialEntity newEntity = new AnoParoquialEntity();
                    newEntity.setAno(year);
                    return newEntity;
                });

        if (AnoParoquialStatus.CONCLUIDO.name().equals(entity.getStatus())) {
            LOGGER.info("Parish Year {} is already CONCLUIDO.", year);
            return;
        }

        if (repository.existsByAnoLessThanAndStatusNot(year, AnoParoquialStatus.CONCLUIDO.name())) {
            throw new IllegalArgumentException(
                    "Existe um ano anterior não concluído. O ano " + year + " não pode ser concluído.");
        }

        entity.setStatus(AnoParoquialStatus.CONCLUIDO.name());
        repository.save(entity);

        int nextYear = year + 1;
        if (repository.findByAno(nextYear).isEmpty()) {
            AnoParoquialEntity nextEntity = new AnoParoquialEntity();
            nextEntity.setAno(nextYear);
            nextEntity.setStatus(AnoParoquialStatus.EM_PLANEJAMENTO.name());
            repository.save(nextEntity);
            LOGGER.info(
                    "Parish Year rollover completed: Year {} set to CONCLUIDO, initialized Year {} as EM_PLANEJAMENTO",
                    year, nextYear);

        } else {
            LOGGER.info(
                    "Parish Year rollover completed: Year {} set to CONCLUIDO. Next year {} was already initialized.",
                    year, nextYear);

        }
    }
}
