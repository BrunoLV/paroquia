package br.com.nsfatima.gestao.calendario.infrastructure.scheduling;

import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.calendario.support.fake.FakeEventoRepository;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.JobLockJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
class GeradorRecorrenciaAnualJobTest {

    private EventoRecorrenciaJpaRepository recorrenciaRepository;
    private FakeEventoRepository eventoRepository;
    private GeradorRecorrenciaAnualJob job;

    @BeforeEach
    void setUp() {
        recorrenciaRepository = mock(EventoRecorrenciaJpaRepository.class);
        JobLockJpaRepository lockRepository = mock(JobLockJpaRepository.class);
        eventoRepository = new FakeEventoRepository();
        job = new GeradorRecorrenciaAnualJob(recorrenciaRepository, eventoRepository, lockRepository);
    }

    @Test
    @DisplayName("Should generate event instances for the whole year")
    void deveGerarAnualInstancias() {
        UUID baseId = UUID.randomUUID();
        EventoEntity baseEvent = new EventoEntity();
        baseEvent.setId(baseId);
        baseEvent.setTitulo("Missa Dominical");
        baseEvent.setGrupoParoquialResponsavelId(UUID.randomUUID());
        baseEvent.setInicioUtc(Instant.parse("2026-01-04T08:00:00Z"));
        baseEvent.setFimUtc(Instant.parse("2026-01-04T09:00:00Z"));
        baseEvent.setStatus("CONFIRMADO");
        eventoRepository.save(baseEvent);

        EventoRecorrenciaEntity recurrence = new EventoRecorrenciaEntity();
        recurrence.setId(UUID.randomUUID());
        recurrence.setEventoBaseId(baseId);
        recurrence.setRegra(new RegraRecorrencia(
                "SEMANAL",
                1,
                List.of(DayOfWeek.SUNDAY),
                null, null, null, null,
                null, null));

        when(recorrenciaRepository.findAll()).thenReturn(List.of(recurrence));

        // Fixed date for test: Jan 1st, 2026
        LocalDate today = LocalDate.of(2026, 1, 1);
        job.executeForYear(today.getYear());

        // 2026 has 52 Sundays. The first one is Jan 4th.
        // All generated events should have the same properties as the base event but different dates.
        assertEquals(52, eventoRepository.count());

        Optional<EventoEntity> first = eventoRepository.findAll().stream()
                .filter(e -> e.getInicioUtc().equals(Instant.parse("2026-01-04T08:00:00Z")))
                .findFirst();
        assertTrue(first.isPresent());
        assertEquals("Missa Dominical", first.get().getTitulo());
    }
}
