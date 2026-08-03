package br.com.nsfatima.gestao.calendario.infrastructure.scheduling;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.JobLockJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Job that runs on January 1st to generate EventoEntity instances for all recurring rules.
 */
@Component
public class GeradorRecorrenciaAnualJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeradorRecorrenciaAnualJob.class);

    private final EventoRecorrenciaJpaRepository recorrenciaRepository;
    private final EventoJpaRepository eventoRepository;
    private final JobLockJpaRepository lockRepository;

    public GeradorRecorrenciaAnualJob(
            EventoRecorrenciaJpaRepository recorrenciaRepository,
            EventoJpaRepository eventoRepository,
            JobLockJpaRepository lockRepository) {
        this.recorrenciaRepository = recorrenciaRepository;
        this.eventoRepository = eventoRepository;
        this.lockRepository = lockRepository;
    }

    /**
     * Executes the generation for the current year. Runs on Jan 1st at 01:00 AM.
     */
    @Scheduled(cron = "0 0 1 1 1 *")
    @Transactional
    public void execute() {
        Instant now = Instant.now();
        if (!lockRepository.acquireLock("YEARLY_RECURRENCE_GENERATOR", now.plus(Duration.ofHours(1)), now)) {
            LOGGER.info("Yearly recurrence generation already running or locked by another instance.");
            return;
        }

        try {
            int year = LocalDate.now(ZoneOffset.UTC).getYear();
            executeForYear(year);
        } finally {
            lockRepository.releaseLock("YEARLY_RECURRENCE_GENERATOR");
        }
    }

    /**
     * Generates instances for a specific year.
     * <p>
     * Usage Example:
     * job.executeForYear(2026);
     */
    @Transactional
    public void executeForYear(int year) {
        LOGGER.info("Starting yearly recurrence generation for year {}", year);
        List<EventoRecorrenciaEntity> rules = recorrenciaRepository.findAll();

        for (EventoRecorrenciaEntity rule : rules) {
            processRule(rule, year);
        }
        LOGGER.info("Finished yearly recurrence generation for year {}", year);
    }

    private void processRule(EventoRecorrenciaEntity ruleEntity, int year) {
        eventoRepository.findById(ruleEntity.getEventoBaseId()).ifPresent(baseEvent -> {
            LocalDate startOfRecurrence = baseEvent.getInicioUtc().atZone(ZoneOffset.UTC).toLocalDate();
            LocalDate endOfYear = LocalDate.of(year, 12, 31);

            List<LocalDate> dates = ruleEntity.getRegra().gerarDatas(startOfRecurrence, endOfYear);

            for (LocalDate date : dates) {
                // Skip the date of the base event to avoid duplication (the base event IS the first occurrence)
                if (date.equals(startOfRecurrence)) {
                    continue;
                }

                if (date.getYear() == year) {
                    generateInstance(baseEvent, date, ruleEntity);
                }
            }
        });
    }

    private void generateInstance(EventoEntity baseEvent, LocalDate date, EventoRecorrenciaEntity ruleEntity) {
        Duration duration = Duration.between(baseEvent.getInicioUtc(), baseEvent.getFimUtc());

        // Map date to original time
        Instant baseDateStart =
                baseEvent.getInicioUtc().atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        Duration timeOfDay = Duration.between(baseDateStart, baseEvent.getInicioUtc());

        Instant startUtc = date.atStartOfDay().toInstant(ZoneOffset.UTC).plus(timeOfDay);
        Instant endUtc = startUtc.plus(duration);

        // Robust de-duplication check: same series and same start time
        if (eventoRepository.existsByRecorrenciaIdAndInicioUtc(ruleEntity.getId(), startUtc)) {
            LOGGER.debug("Instance for {} at {} already exists in series {}, skipping",
                    baseEvent.getTitulo(), startUtc, ruleEntity.getId());

            return;
        }

        EventoEntity instance = new EventoEntity();
        instance.setId(UUID.randomUUID());
        instance.setTitulo(baseEvent.getTitulo());
        instance.setDescricao(baseEvent.getDescricao());
        instance.setCategoria(baseEvent.getCategoria());
        instance.setLocalId(baseEvent.getLocalId());
        instance.setProjetoId(baseEvent.getProjetoId());
        instance.setOrganizacaoResponsavelId(baseEvent.getOrganizacaoResponsavelId());
        instance.setStatus(baseEvent.getStatus());
        instance.setInicioUtc(startUtc);
        instance.setFimUtc(endUtc);
        instance.setRecorrenciaId(ruleEntity.getId());

        eventoRepository.save(instance);
    }
}
