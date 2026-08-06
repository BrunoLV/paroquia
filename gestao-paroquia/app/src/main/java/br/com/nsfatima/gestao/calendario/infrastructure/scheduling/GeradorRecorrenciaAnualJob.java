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
 * Job agendado que roda todo dia 1º de janeiro para materializar, a partir das regras de
 * recorrência já cadastradas ({@link EventoRecorrenciaEntity}), as instâncias de
 * {@link EventoEntity} correspondentes às ocorrências do evento recorrente ao longo do ano
 * corrente. Eventos recorrentes precisam ter suas ocorrências futuras materializadas
 * antecipadamente (em vez de calculadas sob demanda) para que consultas de agenda, conflitos de
 * horário e o fluxo de aprovação operem sobre instâncias concretas de evento como qualquer outro
 * evento do calendário. Usa um lock distribuído ({@code JobLockJpaRepository}) para evitar
 * execução concorrente por múltiplas instâncias da aplicação.
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
     * Executa a geração de ocorrências para o ano corrente. Agendado para rodar todo 1º de
     * janeiro à 01:00. Adquire um lock distribuído antes de executar para evitar processamento
     * duplicado caso mais de uma instância da aplicação esteja rodando; se o lock não puder ser
     * adquirido (já em execução em outra instância), a execução é ignorada silenciosamente. O
     * lock é sempre liberado ao final, mesmo em caso de falha.
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
     * Gera as instâncias de evento recorrente para um ano específico, percorrendo todas as regras
     * de recorrência cadastradas e materializando, para cada uma, as datas de ocorrência que caem
     * dentro do ano informado.
     *
     * <p>Exemplo de uso: {@code job.executeForYear(2026);}
     *
     * @param year ano para o qual as ocorrências recorrentes devem ser geradas
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
        instance.setGrupoParoquialResponsavelId(baseEvent.getGrupoParoquialResponsavelId());
        instance.setStatus(baseEvent.getStatus());
        instance.setInicioUtc(startUtc);
        instance.setFimUtc(endUtc);
        instance.setRecorrenciaId(ruleEntity.getId());

        eventoRepository.save(instance);
    }
}
