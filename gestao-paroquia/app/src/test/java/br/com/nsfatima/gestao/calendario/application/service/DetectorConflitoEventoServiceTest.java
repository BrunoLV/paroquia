package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.application.dto.ConflitoEventoItemDto;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException.ConflitoDetalhe;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaConflitoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DetectorConflitoEventoServiceTest {

    private ReservaConflitoService reservaConflitoService;
    private EventoJpaRepository eventoJpaRepository;
    private DetectorConflitoEventoService detector;

    @BeforeEach
    void setUp() {
        reservaConflitoService = mock(ReservaConflitoService.class);
        eventoJpaRepository = mock(EventoJpaRepository.class);
        detector = new DetectorConflitoEventoService(reservaConflitoService, eventoJpaRepository);
    }

    @Test
    @DisplayName("Deve retornar lista vazia se datas forem nulas")
    void deveRetornarVazioSeDatasNulas() {
        List<ConflitoEventoItemDto> resultado = detector.detectarConflitos(
                null, null, UUID.randomUUID(), List.of(), CategoriaEvento.PASTORAL, null);
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Deve detectar choques de local, ativo e agenda eclesial sem lançar exceção")
    void deveDetectarChoquesCompletos() {
        Instant inicio = Instant.parse("2026-10-10T10:00:00Z");
        Instant fim = Instant.parse("2026-10-10T12:00:00Z");
        UUID localId = UUID.randomUUID();
        UUID ativoId = UUID.randomUUID();

        when(reservaConflitoService.detectarConflitos(eq(TipoItemReserva.LOCAL), eq(localId), any(), any(), any()))
                .thenReturn(List.of(new ConflitoDetalhe(
                        localId, "Salão Paroquial", TipoItemReserva.LOCAL, inicio, fim, UUID.randomUUID(), "João", "Reunião"
                )));

        when(reservaConflitoService.detectarConflitos(eq(TipoItemReserva.ATIVO), eq(ativoId), any(), any(), any()))
                .thenReturn(List.of(new ConflitoDetalhe(
                        ativoId, "Projetor HDMI", TipoItemReserva.ATIVO, inicio, fim, UUID.randomUUID(), "Maria", "Palestra"
                )));

        EventoEntity existente = new EventoEntity();
        existente.setId(UUID.randomUUID());
        existente.setTitulo("Missa Comunitária");
        existente.setCategoria(CategoriaEvento.LITURGICO);
        existente.setInicioUtc(inicio);
        existente.setFimUtc(fim);
        existente.setLocalId(localId);

        when(eventoJpaRepository.findOverlappingEvents(inicio, fim, null))
                .thenReturn(List.of(existente));

        List<ConflitoEventoItemDto> conflitos = detector.detectarConflitos(
                inicio, fim, localId, List.of(ativoId), CategoriaEvento.PASTORAL, null);

        assertThat(conflitos).hasSize(3);
        assertThat(conflitos).anyMatch(c -> "LOCAL".equals(c.tipoConflito()));
        assertThat(conflitos).anyMatch(c -> "ATIVO".equals(c.tipoConflito()));
        assertThat(conflitos).anyMatch(c -> "AGENDA_ECLESIAL".equals(c.tipoConflito()));
    }

    @Test
    @DisplayName("Deve usar buffer zerado para formato pré-definido (ex: MANHA)")
    void deveUsarBufferZeroParaPeriodoPredefinido() {
        Instant inicio = Instant.parse("2026-10-10T09:00:00Z");
        Instant fim = Instant.parse("2026-10-10T14:59:59Z");
        UUID localId = UUID.randomUUID();

        ArgumentCaptor<IntervaloReserva> captor = ArgumentCaptor.forClass(IntervaloReserva.class);
        when(reservaConflitoService.detectarConflitos(eq(TipoItemReserva.LOCAL), eq(localId), any(), captor.capture(), any()))
                .thenReturn(List.of());

        detector.detectarConflitos(inicio, fim, localId, List.of(), CategoriaEvento.PASTORAL, null, TipoHorarioEvento.MANHA);

        assertThat(captor.getValue().bufferMinutos()).isZero();
    }

    @Test
    @DisplayName("Deve usar buffer de 30 min para formato PERSONALIZADO")
    void deveUsarBufferPadraoParaPersonalizado() {
        Instant inicio = Instant.parse("2026-10-10T10:00:00Z");
        Instant fim = Instant.parse("2026-10-10T11:00:00Z");
        UUID localId = UUID.randomUUID();

        ArgumentCaptor<IntervaloReserva> captor = ArgumentCaptor.forClass(IntervaloReserva.class);
        when(reservaConflitoService.detectarConflitos(eq(TipoItemReserva.LOCAL), eq(localId), any(), captor.capture(), any()))
                .thenReturn(List.of());

        detector.detectarConflitos(inicio, fim, localId, List.of(), CategoriaEvento.PASTORAL, null, TipoHorarioEvento.PERSONALIZADO);

        assertThat(captor.getValue().bufferMinutos()).isEqualTo(30);
    }
}
