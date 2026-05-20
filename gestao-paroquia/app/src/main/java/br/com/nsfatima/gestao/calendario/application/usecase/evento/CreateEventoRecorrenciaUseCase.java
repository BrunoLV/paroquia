package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoRecorrenciaResponse;
import br.com.nsfatima.gestao.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.gestao.calendario.domain.policy.CalendarLockPolicy;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.scheduling.YearlyRecurrenceGeneratorJob;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case responsible for registering a recurrence rule for an existing event and generating instances.
 */
@Service
public class CreateEventoRecorrenciaUseCase {

    private final EventoRecorrenciaJpaRepository recurrenceRepository;
    private final EventoJpaRepository eventRepository;
    private final YearlyRecurrenceGeneratorJob generationJob;
    private final CalendarLockPolicy calendarLockPolicy;

    public CreateEventoRecorrenciaUseCase(
            EventoRecorrenciaJpaRepository recurrenceRepository,
            EventoJpaRepository eventRepository,
            YearlyRecurrenceGeneratorJob generationJob,
            CalendarLockPolicy calendarLockPolicy) {
        this.recurrenceRepository = recurrenceRepository;
        this.eventRepository = eventRepository;
        this.generationJob = generationJob;
        this.calendarLockPolicy = calendarLockPolicy;
    }

    /**
     * Transforms a single event into a recurring pattern, automatically populating the future calendar to improve planning efficiency.
     * 
     * Usage Example:
     * {@code
     * useCase.execute(eventoId, new RegraRecorrencia("WEEKLY", 1, ...));
     * }
     */
    @Transactional
    public EventoRecorrenciaResponse execute(UUID eventoId, RegraRecorrencia regra) {
        EventoEntity baseEvent = validateEvent(eventoId);
        calendarLockPolicy.checkLock(baseEvent.getInicioUtc(), EventoStatusInput.valueOf(baseEvent.getStatus()));

        EventoRecorrenciaEntity entity = new EventoRecorrenciaEntity();
        entity.setId(UUID.randomUUID());
        entity.setEventoBaseId(eventoId);
        entity.setRegra(regra);
        recurrenceRepository.save(entity);

        // Immediate generation for current year
        generationJob.executeForYear(LocalDate.now(ZoneOffset.UTC).getYear());

        return new EventoRecorrenciaResponse(
                entity.getId(),
                eventoId,
                regra.frequencia(),
                regra.intervalo());
    }

    private EventoEntity validateEvent(UUID eventoId) {
        return eventRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNotFoundException(eventoId));
    }
}

