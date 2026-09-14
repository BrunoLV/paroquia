package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoRecorrenciaResponse;
import br.com.nsfatima.gestao.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.gestao.calendario.domain.policy.BloqueioAnoParoquialPolicy;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.scheduling.GeradorRecorrenciaAnualJob;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.gestao.projeto.application.service.ValidadorVinculoProjeto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Use case responsible for registering a recurrence rule for an existing event and generating instances.
 */
@Service
public class CriarEventoRecorrenciaUseCase {

    private final EventoRecorrenciaJpaRepository recurrenceRepository;
    private final EventoJpaRepository eventRepository;
    private final GeradorRecorrenciaAnualJob generationJob;
    private final BloqueioAnoParoquialPolicy bloqueioAnoParoquialPolicy;
    private final AnoParoquialJpaRepository anoParoquialRepository;
    private final ValidadorVinculoProjeto validadorVinculoProjeto;

    public CriarEventoRecorrenciaUseCase(
            EventoRecorrenciaJpaRepository recurrenceRepository,
            EventoJpaRepository eventRepository,
            GeradorRecorrenciaAnualJob generationJob,
            BloqueioAnoParoquialPolicy bloqueioAnoParoquialPolicy,
            AnoParoquialJpaRepository anoParoquialRepository,
            ValidadorVinculoProjeto validadorVinculoProjeto) {
        this.recurrenceRepository = recurrenceRepository;
        this.eventRepository = eventRepository;
        this.generationJob = generationJob;
        this.bloqueioAnoParoquialPolicy = bloqueioAnoParoquialPolicy;
        this.anoParoquialRepository = anoParoquialRepository;
        this.validadorVinculoProjeto = validadorVinculoProjeto;
    }

    /**
     * Transforms a single event into a recurring pattern, automatically populating the future calendar to improve
     * planning efficiency.
     * <p>
     * Usage Example:
     * {@code
     * useCase.execute(eventoId, new RegraRecorrencia("WEEKLY", 1, ...));
     * }
     */
    @Transactional
    public EventoRecorrenciaResponse execute(UUID eventoId, RegraRecorrencia regra) {
        EventoEntity baseEvent = validateEvent(eventoId);
        AnoParoquialStatus statusAno = anoParoquialRepository.findByAno(
                        baseEvent.getInicioUtc().atZone(ZoneOffset.UTC).getYear())
                .map(ano -> ano.getStatus())
                .orElse(null);
        bloqueioAnoParoquialPolicy.assertPermitido(
                baseEvent.getInicioUtc(), statusAno, EventoStatusInput.valueOf(baseEvent.getStatus()));

        // Adjust base event to the first valid occurrence of the rule
        adjustBaseEventToFirstOccurrence(baseEvent, regra);

        if (baseEvent.getProjetoId() != null) {
            validadorVinculoProjeto.validarRecorrencia(baseEvent.getProjetoId(),
                    baseEvent.getInicioUtc(), baseEvent.getFimUtc(), regra);

        }

        EventoRecorrenciaEntity entity = new EventoRecorrenciaEntity();
        entity.setId(UUID.randomUUID());
        entity.setEventoBaseId(eventoId);
        entity.setRegra(regra);
        recurrenceRepository.save(entity);

        // Link base event to the recurrence series
        baseEvent.setRecorrenciaId(entity.getId());
        eventRepository.save(baseEvent);

        // Immediate generation for current year
        generationJob.executeForYear(LocalDate.now(ZoneOffset.UTC).getYear());

        return new EventoRecorrenciaResponse(
                entity.getId(),
                eventoId,
                regra.frequencia(),
                regra.intervalo());
    }

    private void adjustBaseEventToFirstOccurrence(EventoEntity baseEvent, RegraRecorrencia regra) {
        LocalDate startOfSeries = baseEvent.getInicioUtc().atZone(ZoneOffset.UTC).toLocalDate();
        List<LocalDate> validDates = regra.gerarDatas(startOfSeries, startOfSeries.plusYears(1));

        if (!validDates.isEmpty()) {
            LocalDate firstValidDate = validDates.getFirst();
            if (!firstValidDate.equals(startOfSeries)) {
                // Move base event to the first valid date
                Duration duration = Duration.between(baseEvent.getInicioUtc(), baseEvent.getFimUtc());

                Instant baseDateStart = startOfSeries.atStartOfDay().toInstant(ZoneOffset.UTC);
                Duration timeOfDay = Duration.between(baseDateStart, baseEvent.getInicioUtc());

                Instant newStartUtc = firstValidDate.atStartOfDay().toInstant(ZoneOffset.UTC).plus(timeOfDay);
                Instant newEndUtc = newStartUtc.plus(duration);

                baseEvent.setInicioUtc(newStartUtc);
                baseEvent.setFimUtc(newEndUtc);
            }
        }
    }

    private EventoEntity validateEvent(UUID eventoId) {
        return eventRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNotFoundException(eventoId));
    }
}
