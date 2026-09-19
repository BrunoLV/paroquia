package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaConflitoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventoReservaIntegrationServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaConflitoService conflitoService;
    @Mock private PublicadorReservaDeAtivo publicadorReservaDeAtivo;

    private EventoReservaIntegrationService service() {
        return new EventoReservaIntegrationService(reservaRepository, conflitoService, publicadorReservaDeAtivo);
    }

    private Reserva reservaExistente(TipoItemReserva tipo, UUID itemId, UUID eventoId, StatusReserva status) {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 0);
        return Reserva.criar(ReservaId.gerar(), tipo, itemId, intervalo, "desc",
                UUID.randomUUID(), UUID.randomUUID(), eventoId, null, status, Instant.now());
    }

    @Test
    @DisplayName("criarReservasEvento retorna lista vazia quando início ou fim são nulos")
    void criarReservasEventoRetornaVazioSemDatas() {
        assertThat(service().criarReservasEvento(UUID.randomUUID(), "Missa", UUID.randomUUID(),
                List.of(), UUID.randomUUID(), UUID.randomUUID(), null, Instant.now())).isEmpty();
        verifyNoInteractions(reservaRepository, conflitoService, publicadorReservaDeAtivo);
    }

    @Test
    @DisplayName("criarReservasEvento retorna vazio quando nenhum item é informado")
    void criarReservasEventoRetornaVazioSemItens() {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant fim = inicio.plus(1, ChronoUnit.HOURS);
        List<Reserva> resultado = service().criarReservasEvento(
                UUID.randomUUID(), "Missa", null, null, UUID.randomUUID(), UUID.randomUUID(), inicio, fim);
        assertThat(resultado).isEmpty();
        verify(reservaRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("criarReservasEvento cria reserva de local e de ativos, valida disponibilidade e publica eventos")
    void criarReservasEventoCriaLocalEAtivos() {
        UUID eventoId = UUID.randomUUID();
        UUID localId = UUID.randomUUID();
        UUID ativoId1 = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID pastoralId = UUID.randomUUID();
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant fim = inicio.plus(1, ChronoUnit.HOURS);

        List<Reserva> salvas = List.of(
                reservaExistente(TipoItemReserva.LOCAL, localId, eventoId, StatusReserva.CONFIRMADA),
                reservaExistente(TipoItemReserva.ATIVO, ativoId1, eventoId, StatusReserva.CONFIRMADA));
        when(reservaRepository.saveAll(any())).thenReturn(salvas);

        List<Reserva> resultado = service().criarReservasEvento(
                eventoId, "Missa", localId, java.util.Arrays.asList(ativoId1, null),
                solicitanteId, pastoralId, inicio, fim);

        assertThat(resultado).hasSize(2);
        verify(conflitoService).validarDisponibilidadeComLock(
                eq(TipoItemReserva.LOCAL), eq(localId), anyString(), any(), isNull());
        verify(conflitoService).validarDisponibilidadeComLock(
                eq(TipoItemReserva.ATIVO), eq(ativoId1), anyString(), any(), isNull());
        verify(publicadorReservaDeAtivo).publicarParaAtivo(ativoId1);
        verify(publicadorReservaDeAtivo, never()).publicarParaAtivo(localId);
    }

    @Test
    @DisplayName("criarReservasEvento usa ids gerados quando solicitante e pastoral não são informados")
    void criarReservasEventoGeraIdsQuandoNulos() {
        UUID eventoId = UUID.randomUUID();
        UUID localId = UUID.randomUUID();
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant fim = inicio.plus(1, ChronoUnit.HOURS);

        ArgumentCaptor<List<Reserva>> captor = ArgumentCaptor.forClass(List.class);
        when(reservaRepository.saveAll(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service().criarReservasEvento(eventoId, "Missa", localId, null, null, null, inicio, fim);

        Reserva criada = captor.getValue().get(0);
        assertThat(criada.getSolicitanteId()).isNotNull();
        assertThat(criada.getPastoralMovimentoId()).isNotNull();
        assertThat(criada.getStatus()).isEqualTo(StatusReserva.CONFIRMADA);
    }

    @Test
    @DisplayName("atualizarReservasEvento não faz nada quando eventoId, início ou fim são nulos")
    void atualizarReservasEventoNoOpQuandoDadosFaltantes() {
        service().atualizarReservasEvento(null, "t", UUID.randomUUID(), List.of(),
                UUID.randomUUID(), UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(60));
        verifyNoInteractions(reservaRepository);
    }

    @Test
    @DisplayName("atualizarReservasEvento cancela reservas de itens desvinculados e mantém/atualiza os demais")
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
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service().atualizarReservasEvento(
                eventoId, "Missa", localMantidoId, List.of(), operadorId, UUID.randomUUID(), inicio, fim);

        assertThat(reservaAtivoRemovido.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(reservaLocalExistente.getStatus()).isEqualTo(StatusReserva.CONFIRMADA);
        assertThat(reservaLocalExistente.getIntervalo().dataHoraInicio()).isEqualTo(inicio);
        verify(publicadorReservaDeAtivo).publicarParaAtivo(ativoRemovidoId);
    }

    @Test
    @DisplayName("atualizarReservasEvento cria nova reserva de local e ativo quando não existem previamente")
    void atualizarReservasEventoCriaNovasReservas() {
        UUID eventoId = UUID.randomUUID();
        UUID localId = UUID.randomUUID();
        UUID ativoId = UUID.randomUUID();
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        Instant fim = inicio.plus(1, ChronoUnit.HOURS);

        when(reservaRepository.findByEventoId(eventoId)).thenReturn(List.of());

        service().atualizarReservasEvento(
                eventoId, "Missa", localId, List.of(ativoId), UUID.randomUUID(), UUID.randomUUID(), inicio, fim);

        verify(reservaRepository, times(2)).save(any());
        verify(publicadorReservaDeAtivo).publicarParaAtivo(ativoId);
    }

    @Test
    @DisplayName("cancelarReservasEventoEmCascata não faz nada quando eventoId é nulo")
    void cancelarReservasEventoEmCascataNoOpQuandoEventoIdNulo() {
        service().cancelarReservasEventoEmCascata(null, "titulo", UUID.randomUUID());
        verifyNoInteractions(reservaRepository, publicadorReservaDeAtivo);
    }

    @Test
    @DisplayName("cancelarReservasEventoEmCascata cancela apenas reservas que ocupam agenda")
    void cancelarReservasEventoEmCascataCancelaApenasAtivas() {
        UUID eventoId = UUID.randomUUID();
        Reserva ativa = reservaExistente(TipoItemReserva.LOCAL, UUID.randomUUID(), eventoId, StatusReserva.CONFIRMADA);
        Reserva jaCancelada =
                reservaExistente(TipoItemReserva.ATIVO, UUID.randomUUID(), eventoId, StatusReserva.PENDENTE);
        jaCancelada.cancelar(UUID.randomUUID(), "motivo qualquer suficiente", false, Instant.now());

        when(reservaRepository.findByEventoId(eventoId)).thenReturn(List.of(ativa, jaCancelada));

        service().cancelarReservasEventoEmCascata(eventoId, "Missa Cancelada", UUID.randomUUID());

        assertThat(ativa.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        verify(reservaRepository, times(1)).save(ativa);
        verify(reservaRepository, never()).save(jaCancelada);
    }
}
