package br.com.nsfatima.gestao.calendario.infrastructure.scheduling;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.application.dto.ConflitoEventoItemDto;
import br.com.nsfatima.gestao.calendario.application.service.DetectorConflitoEventoService;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.PapelEnvolvido;
import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoLocalJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.calendario.support.fake.FakeAprovacaoRepository;
import br.com.nsfatima.gestao.calendario.support.fake.FakeEventoRepository;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.JobLockJpaRepository;
import br.com.nsfatima.gestao.reserva.application.usecase.EventoReservaIntegrationService;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeradorRecorrenciaAnualJobTest {

    private static final ZoneId BRT = ZoneId.of("America/Sao_Paulo");

    private EventoRecorrenciaJpaRepository recorrenciaRepository;
    private FakeEventoRepository eventoRepository;
    private FakeAprovacaoRepository aprovacaoRepository;
    private EventoLocalJpaRepository eventoLocalRepository;
    private EventoEnvolvidoJpaRepository envolvidoRepository;
    private ReservaRepository reservaRepository;
    private DetectorConflitoEventoService detector;
    private EventoReservaIntegrationService reservaIntegration;
    private JobLockJpaRepository lockRepository;

    @BeforeEach
    void setUp() {
        recorrenciaRepository = mock(EventoRecorrenciaJpaRepository.class);
        lockRepository = mock(JobLockJpaRepository.class);
        eventoLocalRepository = mock(EventoLocalJpaRepository.class);
        envolvidoRepository = mock(EventoEnvolvidoJpaRepository.class);
        reservaRepository = mock(ReservaRepository.class);
        detector = mock(DetectorConflitoEventoService.class);
        reservaIntegration = mock(EventoReservaIntegrationService.class);
        eventoRepository = spy(new FakeEventoRepository());
        aprovacaoRepository = new FakeAprovacaoRepository();
    }

    private GeradorRecorrenciaAnualJob jobEm(String instanteUtc) {
        RelogioNegocio relogio = new RelogioNegocio(Clock.fixed(Instant.parse(instanteUtc), ZoneOffset.UTC), BRT);
        return new GeradorRecorrenciaAnualJob(recorrenciaRepository, eventoRepository, lockRepository,
                aprovacaoRepository, eventoLocalRepository, envolvidoRepository, reservaRepository, detector,
                reservaIntegration, relogio);
    }

    private EventoEntity eventoBase(String inicioUtc, String fimUtc) {
        EventoEntity base = new EventoEntity();
        base.setId(UUID.randomUUID());
        base.setTitulo("Missa Dominical");
        base.setOrganizacaoResponsavelId(UUID.randomUUID());
        base.setCategoria(CategoriaEvento.LITURGICO);
        base.setInicioUtc(Instant.parse(inicioUtc));
        base.setFimUtc(Instant.parse(fimUtc));
        base.setStatus("CONFIRMADO");
        eventoRepository.save(base);
        return base;
    }

    private EventoRecorrenciaEntity semanal(EventoEntity base, DayOfWeek dia) {
        EventoRecorrenciaEntity recurrence = new EventoRecorrenciaEntity();
        recurrence.setId(UUID.randomUUID());
        recurrence.setEventoBaseId(base.getId());
        recurrence.setRegra(new RegraRecorrencia("SEMANAL", 1, List.of(dia), null, null, null, null, null, null));
        when(recorrenciaRepository.findAll()).thenReturn(List.of(recurrence));
        return recurrence;
    }

    @Test
    @DisplayName("Should generate event instances for the whole year")
    void deveGerarAnualInstancias() {
        EventoEntity base = eventoBase("2026-01-04T08:00:00Z", "2026-01-04T09:00:00Z");
        semanal(base, DayOfWeek.SUNDAY);

        jobEm("2026-01-01T04:00:00Z").executeForYear(2026);

        assertThat(eventoRepository.count()).isEqualTo(52);
        assertThat(eventoRepository.findAll())
                .anyMatch(e -> e.getInicioUtc().equals(Instant.parse("2026-01-04T08:00:00Z"))
                        && "Missa Dominical".equals(e.getTitulo()));
    }

    @Test
    @DisplayName("Nao deve materializar ocorrencia pendente de aprovacao")
    void naoDeveMaterializarOcorrenciaPendente() {
        EventoEntity base = eventoBase("2026-01-04T08:00:00Z", "2026-01-04T09:00:00Z");
        semanal(base, DayOfWeek.SUNDAY);
        AprovacaoEntity ocorrenciaPendente = new AprovacaoEntity();
        ocorrenciaPendente.setId(UUID.randomUUID());
        ocorrenciaPendente.setEventoId(base.getId());
        ocorrenciaPendente.setDataOcorrenciaUtc(Instant.parse("2026-01-11T08:00:00Z"));
        ocorrenciaPendente.setStatus(AprovacaoStatus.PENDENTE);
        aprovacaoRepository.save(ocorrenciaPendente);

        jobEm("2026-01-01T04:00:00Z").executeForYear(2026);

        assertThat(eventoRepository.count()).isEqualTo(51);
        assertThat(eventoRepository.findAll())
                .noneMatch(e -> e.getInicioUtc().equals(Instant.parse("2026-01-11T08:00:00Z")));
    }

    @Test
    @DisplayName("Ocorrencia que conflita com a agenda nao e materializada nem reserva espaco")
    void ocorrenciaComConflitoNaoESemMaterializada() {
        EventoEntity base = eventoBase("2026-01-04T08:00:00Z", "2026-01-04T09:00:00Z");
        semanal(base, DayOfWeek.SUNDAY);
        Instant domingoEmConflito = Instant.parse("2026-01-11T08:00:00Z");
        when(detector.detectarConflitos(eq(domingoEmConflito), any(), any(), any(), any(), any(), any(), any(),
                any(), any())).thenReturn(List.of(mock(ConflitoEventoItemDto.class)));

        jobEm("2026-01-01T04:00:00Z").executeForYear(2026);

        assertThat(eventoRepository.count()).isEqualTo(51);
        assertThat(eventoRepository.findAll()).noneMatch(e -> e.getInicioUtc().equals(domingoEmConflito));
        verify(reservaIntegration, never()).criarReservasEvento(any(), any(), any(), any(), any(), any(), any(),
                eq(domingoEmConflito), any(), any());
    }

    @Test
    @DisplayName("Instancia materializada herda ativos e envolvidos, passa pelo detector e cria a reserva")
    void instanciaHerdaAtivosEnvolvidosEReservaLocal() {
        EventoEntity base = eventoBase("2026-01-04T08:00:00Z", "2026-01-04T09:00:00Z");
        UUID localId = UUID.randomUUID();
        UUID ativoId = UUID.randomUUID();
        UUID orgApoio = UUID.randomUUID();
        base.setLocalId(localId);
        semanal(base, DayOfWeek.SUNDAY);
        EventoEnvolvidoEntity apoio = new EventoEnvolvidoEntity();
        apoio.setEventoId(base.getId());
        apoio.setOrganizacaoId(orgApoio);
        apoio.setPapelParticipacao(PapelEnvolvido.APOIO);
        when(envolvidoRepository.findByEventoId(base.getId())).thenReturn(List.of(apoio));
        Reserva reservaDeAtivo = Reserva.criar(ReservaId.gerar(), TipoItemReserva.ATIVO, ativoId,
                IntervaloReserva.de(base.getInicioUtc(), base.getFimUtc()), "ativo do evento", UUID.randomUUID(),
                UUID.randomUUID(), base.getId(), null, StatusReserva.CONFIRMADA, base.getInicioUtc());
        when(reservaRepository.findByEventoId(base.getId())).thenReturn(List.of(reservaDeAtivo));

        jobEm("2026-01-01T04:00:00Z").executeForYear(2026);

        verify(detector, times(51)).detectarConflitos(any(), any(), eq(localId), any(), eq(List.of(ativoId)),
                eq(CategoriaEvento.LITURGICO), any(), any(), any(), any());
        ArgumentCaptor<UUID> eventoId = ArgumentCaptor.forClass(UUID.class);
        verify(reservaIntegration, times(51)).criarReservasEvento(eventoId.capture(), eq("Missa Dominical"),
                eq(localId), any(), eq(List.of(ativoId)), any(), eq(base.getOrganizacaoResponsavelId()), any(),
                any(), any());
        assertThat(eventoId.getAllValues()).doesNotContain(base.getId()).doesNotHaveDuplicates();
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EventoEnvolvidoEntity>> copias = ArgumentCaptor.forClass(List.class);
        verify(envolvidoRepository, times(51)).saveAll(copias.capture());
        assertThat(copias.getValue()).singleElement().satisfies(c -> {
            assertThat(c.getOrganizacaoId()).isEqualTo(orgApoio);
            assertThat(c.getPapelParticipacao()).isEqualTo(PapelEnvolvido.APOIO);
            assertThat(c.getEventoId()).isNotEqualTo(base.getId());
        });
    }

    @Test
    @DisplayName("Consulta as ocorrencias existentes e as aprovacoes uma vez por serie, sem N+1 por data")
    void naoFazConsultaPorData() {
        EventoEntity base = eventoBase("2026-01-04T08:00:00Z", "2026-01-04T09:00:00Z");
        EventoRecorrenciaEntity regra = semanal(base, DayOfWeek.SUNDAY);

        jobEm("2026-01-01T04:00:00Z").executeForRule(regra, 2026);

        verify(eventoRepository, times(1)).findByRecorrenciaId(regra.getId());
        verify(reservaRepository, times(1)).findByEventoId(base.getId());
        verify(envolvidoRepository, times(1)).findByEventoId(base.getId());
    }

    @Test
    @DisplayName("Rodar duas vezes nao duplica ocorrencias ja materializadas")
    void naoDuplicaOcorrencias() {
        EventoEntity base = eventoBase("2026-01-04T08:00:00Z", "2026-01-04T09:00:00Z");
        EventoRecorrenciaEntity regra = semanal(base, DayOfWeek.SUNDAY);
        base.setRecorrenciaId(regra.getId());
        GeradorRecorrenciaAnualJob job = jobEm("2026-01-01T04:00:00Z");

        job.executeForRule(regra, 2026);
        job.executeForRule(regra, 2026);

        assertThat(eventoRepository.count()).isEqualTo(52);
    }

    @Test
    @DisplayName("execute() usa o ano de Brasilia: 31/12 22h BRT (01h UTC de 01/01) ainda gera o ano que termina")
    void executeUsaOAnoDeBrasilia() {
        EventoEntity base = eventoBase("2026-01-04T08:00:00Z", "2026-01-04T09:00:00Z");
        semanal(base, DayOfWeek.SUNDAY);
        when(lockRepository.acquireLock(anyString(), any(), any())).thenReturn(true);

        jobEm("2027-01-01T01:00:00Z").execute();

        assertThat(eventoRepository.findAll())
                .allMatch(e -> e.getInicioUtc().atZone(BRT).getYear() == 2026);
        assertThat(eventoRepository.count()).isEqualTo(52);
        verify(lockRepository).releaseLock("YEARLY_RECURRENCE_GENERATOR");
    }

    @Test
    @DisplayName("Evento de sabado 22h30 BRT (01h30 UTC de domingo) mantem o dia e o horario locais")
    void mantemDiaEHorarioLocais() {
        EventoEntity base = eventoBase("2026-01-04T01:30:00Z", "2026-01-04T02:30:00Z");
        semanal(base, DayOfWeek.SATURDAY);

        jobEm("2026-01-01T04:00:00Z").executeForYear(2026);

        assertThat(eventoRepository.findAll())
                .anyMatch(e -> e.getInicioUtc().equals(Instant.parse("2026-01-11T01:30:00Z")))
                .allMatch(e -> e.getInicioUtc().atZone(BRT).getDayOfWeek() == DayOfWeek.SATURDAY
                        && e.getInicioUtc().atZone(BRT).getHour() == 22);
    }

    @Test
    @DisplayName("Serie sem evento-base e ignorada sem erro")
    void serieSemBaseIgnorada() {
        EventoRecorrenciaEntity orfa = new EventoRecorrenciaEntity();
        orfa.setId(UUID.randomUUID());
        orfa.setEventoBaseId(UUID.randomUUID());
        when(recorrenciaRepository.findAll()).thenReturn(List.of(orfa));

        jobEm("2026-01-01T04:00:00Z").executeForYear(2026);

        verify(detector, never()).detectarConflitos(any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any());
        assertThat(eventoRepository.count()).isZero();
    }
}
