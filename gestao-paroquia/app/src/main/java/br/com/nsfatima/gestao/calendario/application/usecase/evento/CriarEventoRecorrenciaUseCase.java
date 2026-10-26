package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoRecorrenciaResponse;
import br.com.nsfatima.gestao.calendario.domain.exception.EventoNaoEncontradoException;
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
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
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
    private final RelogioNegocio relogio;

    public CriarEventoRecorrenciaUseCase(
            EventoRecorrenciaJpaRepository recurrenceRepository,
            EventoJpaRepository eventRepository,
            GeradorRecorrenciaAnualJob generationJob,
            BloqueioAnoParoquialPolicy bloqueioAnoParoquialPolicy,
            AnoParoquialJpaRepository anoParoquialRepository,
            ValidadorVinculoProjeto validadorVinculoProjeto,
            RelogioNegocio relogio) {
        this.recurrenceRepository = recurrenceRepository;
        this.eventRepository = eventRepository;
        this.generationJob = generationJob;
        this.bloqueioAnoParoquialPolicy = bloqueioAnoParoquialPolicy;
        this.anoParoquialRepository = anoParoquialRepository;
        this.validadorVinculoProjeto = validadorVinculoProjeto;
        this.relogio = relogio;
    }

    /**
     * Transforma um evento único em um padrão recorrente, populando automaticamente o calendário futuro para
     * melhorar a eficiência do planejamento.
     * <p>
     * Exemplo de uso:
     * {@code
     * useCase.execute(eventoId, new RegraRecorrencia("WEEKLY", 1, ...));
     * }
     */
    @Transactional
    public EventoRecorrenciaResponse execute(UUID eventoId, RegraRecorrencia regra) {
        EventoEntity baseEvent = validateEvent(eventoId);
        AnoParoquialStatus statusAno = anoParoquialRepository.findByAno(
                        relogio.anoDe(baseEvent.getInicioUtc()))
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

        // Gera apenas a série recém-criada, no ano corrente da paróquia
        generationJob.executeForRule(entity, relogio.anoDe(relogio.agora()));

        return new EventoRecorrenciaResponse(
                entity.getId(),
                eventoId,
                regra.frequencia(),
                regra.intervalo());
    }

    private void adjustBaseEventToFirstOccurrence(EventoEntity baseEvent, RegraRecorrencia regra) {
        LocalDate startOfSeries = relogio.dataDe(baseEvent.getInicioUtc());
        List<LocalDate> validDates = regra.gerarDatas(startOfSeries, startOfSeries.plusYears(1));

        if (!validDates.isEmpty() && !validDates.getFirst().equals(startOfSeries)) {
            Duration duration = Duration.between(baseEvent.getInicioUtc(), baseEvent.getFimUtc());
            Instant newStart = relogio.mesmoHorarioNoDia(baseEvent.getInicioUtc(), validDates.getFirst());
            baseEvent.setInicioUtc(newStart);
            baseEvent.setFimUtc(newStart.plus(duration));
        }
    }

    private EventoEntity validateEvent(UUID eventoId) {
        return eventRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNaoEncontradoException(eventoId));
    }
}
