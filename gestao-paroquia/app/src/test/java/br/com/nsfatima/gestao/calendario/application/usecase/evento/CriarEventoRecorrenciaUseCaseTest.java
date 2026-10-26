package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.domain.policy.BloqueioAnoParoquialPolicy;
import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.scheduling.GeradorRecorrenciaAnualJob;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.gestao.projeto.application.service.ValidadorVinculoProjeto;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CriarEventoRecorrenciaUseCaseTest {

    private EventoRecorrenciaJpaRepository recorrenciaRepository;
    private EventoJpaRepository eventoRepository;
    private GeradorRecorrenciaAnualJob job;
    private AnoParoquialJpaRepository anoParoquialRepository;
    private CriarEventoRecorrenciaUseCase useCase;

    @BeforeEach
    void setUp() {
        recorrenciaRepository = mock(EventoRecorrenciaJpaRepository.class);
        eventoRepository = mock(EventoJpaRepository.class);
        job = mock(GeradorRecorrenciaAnualJob.class);
        anoParoquialRepository = mock(AnoParoquialJpaRepository.class);
        RelogioNegocio relogio = new RelogioNegocio(
                Clock.fixed(Instant.parse("2027-01-01T01:00:00Z"), ZoneOffset.UTC), ZoneId.of("America/Sao_Paulo"));
        useCase = new CriarEventoRecorrenciaUseCase(recorrenciaRepository, eventoRepository, job,
                mock(BloqueioAnoParoquialPolicy.class), anoParoquialRepository, mock(ValidadorVinculoProjeto.class),
                relogio);
    }

    private EventoEntity baseEm(String inicioUtc) {
        EventoEntity base = new EventoEntity();
        base.setId(UUID.randomUUID());
        base.setStatus("CONFIRMADO");
        base.setInicioUtc(Instant.parse(inicioUtc));
        base.setFimUtc(Instant.parse(inicioUtc).plusSeconds(3600));
        when(eventoRepository.findById(base.getId())).thenReturn(Optional.of(base));
        return base;
    }

    private RegraRecorrencia semanal(DayOfWeek dia) {
        return new RegraRecorrencia("SEMANAL", 1, List.of(dia), null, null, null, null, null, null);
    }

    @Test
    @DisplayName("Gera apenas a regra recem-criada, no ano de Brasilia (31/12 22h BRT ainda e o ano que termina)")
    void geraSomenteARegraAfetadaNoAnoDeBrasilia() {
        EventoEntity base = baseEm("2026-12-27T12:00:00Z");

        useCase.execute(base.getId(), semanal(DayOfWeek.SUNDAY));

        ArgumentCaptor<EventoRecorrenciaEntity> regra = ArgumentCaptor.forClass(EventoRecorrenciaEntity.class);
        verify(job).executeForRule(regra.capture(), eq(2026));
        assertThat(regra.getValue().getEventoBaseId()).isEqualTo(base.getId());
        verify(job, never()).executeForYear(anyInt());
    }

    @Test
    @DisplayName("Ano paroquial validado e o do dia civil de Brasilia do evento-base")
    void validaAnoParoquialPeloDiaCivilDeBrasilia() {
        EventoEntity base = baseEm("2027-01-01T01:00:00Z");

        useCase.execute(base.getId(), semanal(DayOfWeek.FRIDAY));

        verify(anoParoquialRepository).findByAno(2026);
    }

    @Test
    @DisplayName("Ajuste da primeira ocorrencia preserva o horario local (22h30 BRT) do evento-base")
    void ajustePreservaHorarioLocal() {
        // sabado 03/01/2026 22h30 BRT = 01h30Z de domingo; a serie e de quartas
        EventoEntity base = baseEm("2026-01-04T01:30:00Z");

        useCase.execute(base.getId(), semanal(DayOfWeek.WEDNESDAY));

        assertThat(base.getInicioUtc()).isEqualTo(Instant.parse("2026-01-08T01:30:00Z"));
        assertThat(base.getFimUtc()).isEqualTo(Instant.parse("2026-01-08T02:30:00Z"));
    }
}
