package br.com.nsfatima.gestao.reserva.domain.service;

import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException.ConflitoDetalhe;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReservaConflitoServiceJanelaTest {

    private static final Instant BASE = Instant.parse("2026-05-10T12:00:00Z");

    private final ReservaRepository repository = Mockito.mock(ReservaRepository.class);
    private final ReservaConflitoService service = new ReservaConflitoService(repository);

    private Reserva existente(UUID itemId, Instant inicio, Instant fim, int buffer, UUID eventoId) {
        return Reserva.criar(ReservaId.gerar(), TipoItemReserva.LOCAL, itemId,
                new IntervaloReserva(inicio, fim, buffer), "x", UUID.randomUUID(), UUID.randomUUID(),
                eventoId, null, StatusReserva.CONFIRMADA, BASE);
    }

    @Test
    @DisplayName("A janela de busca acompanha o buffer máximo do intervalo, sem constante própria")
    void janelaDeBuscaUsaBufferMaximo() {
        UUID itemId = UUID.randomUUID();
        IntervaloReserva novo = new IntervaloReserva(BASE, BASE.plus(Duration.ofHours(2)), 240);
        when(repository.findConflitosPotenciaisComLock(any(), any(), any(), any(), any())).thenReturn(List.of());

        service.buscarColisoesComLock(TipoItemReserva.LOCAL, itemId, novo, null);

        ArgumentCaptor<Instant> inicio = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> fim = ArgumentCaptor.forClass(Instant.class);
        verify(repository).findConflitosPotenciaisComLock(
                eq(TipoItemReserva.LOCAL), eq(itemId), inicio.capture(), fim.capture(), any());
        long maximo = IntervaloReserva.BUFFER_MAXIMO_MINUTOS;
        assertThat(inicio.getValue()).isEqualTo(BASE.minus(Duration.ofMinutes(maximo)));
        assertThat(fim.getValue()).isEqualTo(novo.fimComBuffer().plus(Duration.ofMinutes(maximo)));
    }

    @Test
    @DisplayName("Reserva existente com buffer máximo ainda colide quando o novo pedido começa dentro do seu buffer")
    void existenteComBufferMaximoColide() {
        UUID itemId = UUID.randomUUID();
        Reserva grande = existente(itemId, BASE.minus(Duration.ofHours(6)), BASE.minus(Duration.ofHours(5)), 240, null);
        IntervaloReserva novo = new IntervaloReserva(BASE.minus(Duration.ofMinutes(61)), BASE, 0);
        when(repository.findConflitosPotenciaisComLock(any(), any(), any(), any(), any()))
                .thenReturn(List.of(grande));

        List<ConflitoDetalhe> conflitos =
                service.detectarConflitos(TipoItemReserva.LOCAL, itemId, "Salão", novo, null);

        assertThat(conflitos).hasSize(1);
    }

    @Test
    @DisplayName("Reservas do mesmo evento não conflitam entre si (turnos contíguos com margem logística)")
    void ignoraReservasDoMesmoEvento() {
        UUID itemId = UUID.randomUUID();
        UUID eventoId = UUID.randomUUID();
        Reserva manha = existente(itemId, BASE, BASE.plus(Duration.ofHours(6)), 30, eventoId);
        Reserva outroEvento = existente(itemId, BASE, BASE.plus(Duration.ofHours(6)), 30, UUID.randomUUID());
        IntervaloReserva tarde = IntervaloReserva.de(BASE.plus(Duration.ofHours(6)), BASE.plus(Duration.ofHours(12)));
        when(repository.findConflitosPotenciaisComLock(any(), any(), any(), any(), any()))
                .thenReturn(List.of(manha, outroEvento));

        List<ConflitoDetalhe> conflitos =
                service.detectarConflitos(TipoItemReserva.LOCAL, itemId, "Salão", tarde, null, eventoId);

        assertThat(conflitos).hasSize(1);
    }
}
