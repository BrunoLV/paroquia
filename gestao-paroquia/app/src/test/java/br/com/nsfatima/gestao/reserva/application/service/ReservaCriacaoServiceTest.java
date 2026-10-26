package br.com.nsfatima.gestao.reserva.application.service;

import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.application.port.out.ConsultaManutencaoAtivoPort;
import br.com.nsfatima.gestao.reserva.application.port.out.ConsultaManutencaoAtivoPort.JanelaIndisponivel;
import br.com.nsfatima.gestao.reserva.application.port.out.ItemReservavelPort;
import br.com.nsfatima.gestao.reserva.application.port.out.ItemReservavelPort.ItemReservavel;
import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService.ItemRef;
import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService.PedidoReserva;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException.ConflitoDetalhe;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaOperacaoInvalidaException;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaConflitoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaCriacaoServiceTest {

    private static final Instant INICIO = Instant.parse("2026-06-01T13:00:00Z");

    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaConflitoService conflitoService;
    @Mock private ConsultaManutencaoAtivoPort manutencaoPort;
    @Mock private ItemReservavelPort itemPort;
    @Mock private PublicadorReservaDeAtivo publicador;

    private ReservaCriacaoService service() {
        return new ReservaCriacaoService(reservaRepository, conflitoService, manutencaoPort, itemPort, publicador);
    }

    private PedidoReserva pedido(TipoItemReserva tipo, UUID itemId, StatusReserva status) {
        return new PedidoReserva(tipo, itemId, null, IntervaloReserva.de(INICIO, INICIO.plus(Duration.ofHours(1))),
                "finalidade", UUID.randomUUID(), UUID.randomUUID(), null, null, status);
    }

    private void itemDisponivel(TipoItemReserva tipo, UUID itemId, boolean ativo, boolean reservavel) {
        when(itemPort.bloquear(tipo, itemId))
                .thenReturn(Optional.of(new ItemReservavel(itemId, "Item", ativo, reservavel)));
    }

    @Test
    @DisplayName("Pedido sem solicitante ou sem pastoral falha de imediato, sem inventar identidade")
    void pedidoExigeSolicitanteEPastoral() {
        IntervaloReserva intervalo = IntervaloReserva.de(INICIO, INICIO.plusSeconds(3600));
        UUID itemId = UUID.randomUUID();

        assertThatThrownBy(() -> new PedidoReserva(TipoItemReserva.LOCAL, itemId, null, intervalo, "x",
                null, UUID.randomUUID(), null, null, StatusReserva.CONFIRMADA))
                .isInstanceOf(ReservaOperacaoInvalidaException.class);
        assertThatThrownBy(() -> new PedidoReserva(TipoItemReserva.LOCAL, itemId, null, intervalo, "x",
                UUID.randomUUID(), null, null, null, StatusReserva.CONFIRMADA))
                .isInstanceOf(ReservaOperacaoInvalidaException.class);
    }

    @Test
    @DisplayName("Item inexistente, inativo ou não reservável impede a reserva antes de consultar a agenda")
    void itemInvalidoImpedeReserva() {
        UUID inexistente = UUID.randomUUID();
        UUID inativo = UUID.randomUUID();
        UUID naoReservavel = UUID.randomUUID();
        when(itemPort.bloquear(TipoItemReserva.LOCAL, inexistente)).thenReturn(Optional.empty());
        itemDisponivel(TipoItemReserva.LOCAL, inativo, false, true);
        itemDisponivel(TipoItemReserva.ATIVO, naoReservavel, true, false);

        assertThatThrownBy(() -> service().criar(pedido(TipoItemReserva.LOCAL, inexistente, StatusReserva.PENDENTE)))
                .isInstanceOf(ReservaOperacaoInvalidaException.class).hasMessageContaining("não foi encontrado");
        assertThatThrownBy(() -> service().criar(pedido(TipoItemReserva.LOCAL, inativo, StatusReserva.PENDENTE)))
                .isInstanceOf(ReservaOperacaoInvalidaException.class).hasMessageContaining("não está disponível");
        assertThatThrownBy(() -> service().criar(pedido(TipoItemReserva.ATIVO, naoReservavel, StatusReserva.PENDENTE)))
                .isInstanceOf(ReservaOperacaoInvalidaException.class).hasMessageContaining("não é elegível");
        verify(reservaRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Conflito de agenda lança ReservaConflitoException e nada é gravado")
    void conflitoDeAgendaImpedeReserva() {
        UUID itemId = UUID.randomUUID();
        itemDisponivel(TipoItemReserva.LOCAL, itemId, true, true);
        ConflitoDetalhe detalhe = new ConflitoDetalhe(itemId, "Item", TipoItemReserva.LOCAL, INICIO, INICIO,
                UUID.randomUUID(), "x", "y");
        when(conflitoService.detectarConflitos(any(), any(), any(), any(), isNull(), isNull()))
                .thenReturn(List.of(detalhe));

        assertThatThrownBy(() -> service().criar(pedido(TipoItemReserva.LOCAL, itemId, StatusReserva.PENDENTE)))
                .isInstanceOf(ReservaConflitoException.class);
        verify(reservaRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Manutenção do ativo no período impede a reserva por qualquer caminho de criação")
    void manutencaoImpedeReservaDeAtivo() {
        UUID ativoId = UUID.randomUUID();
        itemDisponivel(TipoItemReserva.ATIVO, ativoId, true, true);
        when(conflitoService.detectarConflitos(any(), any(), any(), any(), any(), any())).thenReturn(List.of());
        when(manutencaoPort.janelasDeManutencao(eq(ativoId), any(), any()))
                .thenReturn(List.of(new JanelaIndisponivel(INICIO, INICIO.plusSeconds(60))));

        assertThatThrownBy(() -> service().criar(pedido(TipoItemReserva.ATIVO, ativoId, StatusReserva.CONFIRMADA)))
                .isInstanceOf(ReservaOperacaoInvalidaException.class).hasMessageContaining("manutenção");
        verify(reservaRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Local nunca consulta janelas de manutenção")
    void localNaoConsultaManutencao() {
        UUID localId = UUID.randomUUID();
        itemDisponivel(TipoItemReserva.LOCAL, localId, true, true);
        when(conflitoService.detectarConflitos(any(), any(), any(), any(), any(), any())).thenReturn(List.of());
        when(reservaRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        service().criar(pedido(TipoItemReserva.LOCAL, localId, StatusReserva.PENDENTE));

        verify(manutencaoPort, never()).janelasDeManutencao(any(), any(), any());
    }

    @Test
    @DisplayName("Reserva de ativo confirmada é gravada e notifica o patrimônio; pendente não notifica")
    void publicaAtivoApenasQuandoConfirmada() {
        UUID ativoId = UUID.randomUUID();
        itemDisponivel(TipoItemReserva.ATIVO, ativoId, true, true);
        when(conflitoService.detectarConflitos(any(), any(), any(), any(), any(), any())).thenReturn(List.of());
        when(manutencaoPort.janelasDeManutencao(any(), any(), any())).thenReturn(List.of());
        when(reservaRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        Reserva pendente = service().criar(pedido(TipoItemReserva.ATIVO, ativoId, StatusReserva.PENDENTE));
        verify(publicador, never()).publicarParaAtivo(any());
        assertThat(pendente.getStatus()).isEqualTo(StatusReserva.PENDENTE);

        service().criar(pedido(TipoItemReserva.ATIVO, ativoId, StatusReserva.CONFIRMADA));
        verify(publicador).publicarParaAtivo(ativoId);
    }

    @Test
    @DisplayName("criarTodas bloqueia os itens em ordem determinística antes de validar e grava em lote")
    void criarTodasBloqueiaEmOrdem() {
        UUID menor = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID maior = UUID.fromString("ffffffff-0000-0000-0000-000000000001");
        itemDisponivel(TipoItemReserva.LOCAL, menor, true, true);
        itemDisponivel(TipoItemReserva.LOCAL, maior, true, true);
        when(conflitoService.detectarConflitos(any(), any(), any(), any(), any(), any())).thenReturn(List.of());
        when(reservaRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<Reserva> criadas = service().criarTodas(List.of(
                pedido(TipoItemReserva.LOCAL, maior, StatusReserva.CONFIRMADA),
                pedido(TipoItemReserva.LOCAL, menor, StatusReserva.CONFIRMADA)));

        assertThat(criadas).hasSize(2);
        InOrder ordem = inOrder(itemPort);
        ordem.verify(itemPort).bloquear(TipoItemReserva.LOCAL, menor);
        ordem.verify(itemPort).bloquear(TipoItemReserva.LOCAL, maior);
    }

    @Test
    @DisplayName("bloquearItens ignora repetidos")
    void bloquearItensIgnoraRepetidos() {
        UUID localId = UUID.randomUUID();
        itemDisponivel(TipoItemReserva.LOCAL, localId, true, true);

        service().bloquearItens(List.of(new ItemRef(TipoItemReserva.LOCAL, localId),
                new ItemRef(TipoItemReserva.LOCAL, localId)));

        verify(itemPort).bloquear(TipoItemReserva.LOCAL, localId);
    }
}
