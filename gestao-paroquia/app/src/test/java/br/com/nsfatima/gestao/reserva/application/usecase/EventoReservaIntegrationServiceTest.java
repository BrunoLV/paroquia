package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService;
import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService.PedidoReserva;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaOperacaoInvalidaException;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoReservaIntegrationServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaCriacaoService criacaoService;
    @Mock private PublicadorReservaDeAtivo publicadorReservaDeAtivo;

    private EventoReservaIntegrationService service() {
        return new EventoReservaIntegrationService(reservaRepository, criacaoService, publicadorReservaDeAtivo);
    }

    private Reserva reservaExistente(TipoItemReserva tipo, UUID itemId, UUID eventoId, StatusReserva status) {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 0);
        return Reserva.criar(ReservaId.gerar(), tipo, itemId, intervalo, "desc",
                UUID.randomUUID(), UUID.randomUUID(), eventoId, null, status, Instant.now());
    }

    private Reserva outraReserva(TipoItemReserva tipo, UUID eventoId, StatusReserva status) {
        return reservaExistente(tipo, UUID.randomUUID(), eventoId, status);
    }

    private List<Reserva> criar(UUID eventoId, UUID localId, List<UUID> ativos, UUID solicitante, UUID pastoral,
                                Instant inicio, Instant fim, List<TipoHorarioEvento> periodos) {
        return service().criarReservasEvento(eventoId, "Missa", localId, null, ativos, solicitante, pastoral,
                inicio, fim, periodos);
    }

    @Test
    @DisplayName("criarReservasEvento retorna lista vazia quando início ou fim são nulos")
    void criarReservasEventoRetornaVazioSemDatas() {
        assertThat(criar(UUID.randomUUID(), UUID.randomUUID(), List.of(), UUID.randomUUID(), UUID.randomUUID(),
                null, Instant.now(), null)).isEmpty();
        verifyNoInteractions(reservaRepository, criacaoService, publicadorReservaDeAtivo);
    }

    @Test
    @DisplayName("criarReservasEvento retorna vazio quando nenhum item é informado")
    void criarReservasEventoRetornaVazioSemItens() {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        List<Reserva> resultado = criar(UUID.randomUUID(), null, null, UUID.randomUUID(), UUID.randomUUID(),
                inicio, inicio.plus(1, ChronoUnit.HOURS), null);
        assertThat(resultado).isEmpty();
        verifyNoInteractions(criacaoService);
    }

    @Test
    @DisplayName("criarReservasEvento pede ao serviço único reservas CONFIRMADAS de local e ativos, ignorando nulos")
    @SuppressWarnings("unchecked")
    void criarReservasEventoCriaLocalEAtivos() {
        UUID eventoId = UUID.randomUUID();
        UUID localId = UUID.randomUUID();
        UUID ativoId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID pastoralId = UUID.randomUUID();
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        List<Reserva> salvas = List.of(mock(Reserva.class), mock(Reserva.class));
        when(criacaoService.criarTodas(any())).thenReturn(salvas);

        List<Reserva> resultado = criar(eventoId, localId, Arrays.asList(ativoId, null), solicitanteId, pastoralId,
                inicio, inicio.plus(1, ChronoUnit.HOURS), null);

        assertThat(resultado).isSameAs(salvas);
        ArgumentCaptor<List<PedidoReserva>> pedidos = ArgumentCaptor.forClass(List.class);
        verify(criacaoService).criarTodas(pedidos.capture());
        assertThat(pedidos.getValue()).hasSize(2).allSatisfy(p -> {
            assertThat(p.statusInicial()).isEqualTo(StatusReserva.CONFIRMADA);
            assertThat(p.eventoId()).isEqualTo(eventoId);
            assertThat(p.solicitanteId()).isEqualTo(solicitanteId);
            assertThat(p.pastoralMovimentoId()).isEqualTo(pastoralId);
        });
        assertThat(pedidos.getValue()).extracting(PedidoReserva::tipoItem)
                .containsExactly(TipoItemReserva.LOCAL, TipoItemReserva.ATIVO);
    }

    @Test
    @DisplayName("criarReservasEvento falha de imediato sem solicitante ou pastoral, sem gerar identidades")
    void criarReservasEventoFalhaSemSolicitanteOuPastoral() {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant fim = inicio.plus(1, ChronoUnit.HOURS);
        UUID localId = UUID.randomUUID();

        assertThatThrownBy(() -> criar(UUID.randomUUID(), localId, null, null, UUID.randomUUID(), inicio, fim, null))
                .isInstanceOf(ReservaOperacaoInvalidaException.class);
        assertThatThrownBy(() -> criar(UUID.randomUUID(), localId, null, UUID.randomUUID(), null, inicio, fim, null))
                .isInstanceOf(ReservaOperacaoInvalidaException.class);
        verify(criacaoService, never()).criarTodas(any());
    }

    @Test
    @DisplayName("atualizarReservasEvento não faz nada quando eventoId, início ou fim são nulos")
    void atualizarReservasEventoNoOpQuandoDadosFaltantes() {
        service().atualizarReservasEvento(null, "t", UUID.randomUUID(), null, List.of(),
                UUID.randomUUID(), UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(60), null);
        verifyNoInteractions(reservaRepository, criacaoService);
    }

    @Test
    @DisplayName("atualizarReservasEvento cancela reservas de itens desvinculados e reagenda as dos itens mantidos")
    void atualizarReservasEventoCancelaItensRemovidos() {
        UUID eventoId = UUID.randomUUID();
        UUID localMantidoId = UUID.randomUUID();
        UUID ativoRemovidoId = UUID.randomUUID();
        UUID operadorId = UUID.randomUUID();
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        Instant fim = inicio.plus(1, ChronoUnit.HOURS);

        Reserva reservaLocalExistente = reservaExistente(
                TipoItemReserva.LOCAL, localMantidoId, eventoId, StatusReserva.CONFIRMADA);
        Reserva reservaAtivoRemovido = reservaExistente(
                TipoItemReserva.ATIVO, ativoRemovidoId, eventoId, StatusReserva.CONFIRMADA);
        when(reservaRepository.findByEventoId(eventoId))
                .thenReturn(List.of(reservaLocalExistente, reservaAtivoRemovido));

        service().atualizarReservasEvento(eventoId, "Missa", localMantidoId, null, List.of(), operadorId,
                UUID.randomUUID(), inicio, fim, null);

        assertThat(reservaAtivoRemovido.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(reservaAtivoRemovido.getCanceladoPorId()).isEqualTo(operadorId);
        verify(reservaRepository).save(reservaAtivoRemovido);
        verify(criacaoService).reagendar(any(), any(), any());
        verify(publicadorReservaDeAtivo).publicarParaAtivo(ativoRemovidoId);
    }

    @Test
    @DisplayName("atualizarReservasEvento cria novas reservas pelo serviço único quando não existem previamente")
    void atualizarReservasEventoCriaNovasReservas() {
        UUID eventoId = UUID.randomUUID();
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        when(reservaRepository.findByEventoId(eventoId)).thenReturn(List.of());

        service().atualizarReservasEvento(eventoId, "Missa", UUID.randomUUID(), null, List.of(UUID.randomUUID()),
                UUID.randomUUID(), UUID.randomUUID(), inicio, inicio.plus(1, ChronoUnit.HOURS), null);

        verify(criacaoService, times(2)).criar(any());
        verify(criacaoService, never()).reagendar(any(), any(), any());
    }

    @Test
    @DisplayName("cancelarReservasEventoEmCascata não faz nada quando eventoId é nulo")
    void cancelarReservasEventoEmCascataNoOpQuandoEventoIdNulo() {
        service().cancelarReservasEventoEmCascata(null, "titulo", UUID.randomUUID());
        verifyNoInteractions(reservaRepository, publicadorReservaDeAtivo);
    }

    @Test
    @DisplayName("cancelarReservasEventoEmCascata cancela apenas pendentes e confirmadas, preservando retiradas e finalizadas")
    void cancelarReservasEventoEmCascataCancelaApenasCancelaveis() {
        UUID eventoId = UUID.randomUUID();
        UUID ativoId = UUID.randomUUID();
        Reserva ativa = reservaExistente(TipoItemReserva.ATIVO, ativoId, eventoId, StatusReserva.CONFIRMADA);
        Reserva retirada = outraReserva(TipoItemReserva.ATIVO, eventoId, StatusReserva.CONFIRMADA);
        retirada.registrarRetirada(UUID.randomUUID(), Instant.now());
        Reserva finalizada = outraReserva(TipoItemReserva.LOCAL, eventoId, StatusReserva.CONFIRMADA);
        finalizada.finalizarUsoLocal(Instant.now().plus(5, ChronoUnit.HOURS));
        Reserva jaCancelada = outraReserva(TipoItemReserva.LOCAL, eventoId, StatusReserva.PENDENTE);
        jaCancelada.cancelar(UUID.randomUUID(), "motivo qualquer suficiente", false, Instant.now());
        when(reservaRepository.findByEventoId(eventoId)).thenReturn(List.of(ativa, retirada, finalizada, jaCancelada));

        service().cancelarReservasEventoEmCascata(eventoId, "Missa Cancelada", UUID.randomUUID());

        assertThat(ativa.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(retirada.getStatus()).isEqualTo(StatusReserva.RETIRADO);
        assertThat(finalizada.getStatus()).isEqualTo(StatusReserva.FINALIZADA);
        verify(reservaRepository, times(1)).save(ativa);
        verify(reservaRepository, never()).save(retirada);
        verify(reservaRepository, never()).save(finalizada);
        verify(reservaRepository, never()).save(jaCancelada);
        verify(publicadorReservaDeAtivo).publicarParaAtivo(ativoId);
    }

    @Test
    @DisplayName("cancelarReservasEventoEmCascata exige operador identificado quando há o que cancelar")
    void cancelarEmCascataExigeOperador() {
        UUID eventoId = UUID.randomUUID();
        when(reservaRepository.findByEventoId(eventoId))
                .thenReturn(List.of(reservaExistente(TipoItemReserva.LOCAL, UUID.randomUUID(), eventoId,
                        StatusReserva.CONFIRMADA)));

        assertThatThrownBy(() -> service().cancelarReservasEventoEmCascata(eventoId, "x", null))
                .isInstanceOf(ReservaOperacaoInvalidaException.class);
    }

    @Test
    @DisplayName("criarReservasEvento com múltiplos períodos gera pedidos com sufixo de turno na descrição")
    @SuppressWarnings("unchecked")
    void criarReservasEventoComMultiplosPeriodosGeraDescricoesComTurno() {
        UUID localId = UUID.randomUUID();
        Instant inicio = Instant.parse("2026-10-15T06:00:00Z");
        Instant fim = Instant.parse("2026-10-15T23:59:00Z");

        criar(UUID.randomUUID(), localId, List.of(), UUID.randomUUID(), UUID.randomUUID(), inicio, fim,
                List.of(TipoHorarioEvento.MANHA, TipoHorarioEvento.NOITE));

        ArgumentCaptor<List<PedidoReserva>> captor = ArgumentCaptor.forClass(List.class);
        verify(criacaoService).criarTodas(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue().get(0).descricao()).contains("Manhã");
        assertThat(captor.getValue().get(1).descricao()).contains("Noite");
    }
}
