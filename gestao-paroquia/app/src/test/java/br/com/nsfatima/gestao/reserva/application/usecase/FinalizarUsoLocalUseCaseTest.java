package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoEncontradaException;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.policy.PadraoReservaAutorizacaoPolicy;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinalizarUsoLocalUseCaseTest {

    @Mock private ReservaRepository reservaRepository;

    private final ReservaAuthorizationService authorization =
            new ReservaAuthorizationService(new PadraoReservaAutorizacaoPolicy());

    private FinalizarUsoLocalUseCase useCase() {
        return new FinalizarUsoLocalUseCase(reservaRepository, authorization);
    }

    private Reserva localEmUso(UUID solicitanteId, UUID pastoralId) {
        Instant inicio = Instant.now().minus(Duration.ofHours(3));
        return Reserva.criar(ReservaId.gerar(), TipoItemReserva.LOCAL, UUID.randomUUID(),
                new IntervaloReserva(inicio, inicio.plus(Duration.ofHours(2)), 0), "culto", solicitanteId, pastoralId,
                null, null, StatusReserva.CONFIRMADA, inicio.minus(Duration.ofDays(2)));
    }

    @Test
    @DisplayName("O solicitante finaliza o uso do local depois de iniciado")
    void solicitanteFinalizaUso() {
        UUID solicitante = UUID.randomUUID();
        Reserva r = localEmUso(solicitante, UUID.randomUUID());
        when(reservaRepository.findById(r.getId())).thenReturn(Optional.of(r));
        when(reservaRepository.save(r)).thenReturn(r);

        Reserva resultado = useCase().executar(r.getId().value(),
                new UsuarioActor(solicitante, "x", List.of(), List.of(), List.of()));

        assertThat(resultado.getStatus()).isEqualTo(StatusReserva.FINALIZADA);
    }

    @Test
    @DisplayName("O coordenador da pastoral finaliza o uso do local")
    void coordenadorFinalizaUso() {
        UUID pastoral = UUID.randomUUID();
        Reserva r = localEmUso(UUID.randomUUID(), pastoral);
        when(reservaRepository.findById(r.getId())).thenReturn(Optional.of(r));
        when(reservaRepository.save(r)).thenReturn(r);

        useCase().executar(r.getId().value(),
                new UsuarioActor(UUID.randomUUID(), "c", List.of(), List.of(pastoral), List.of(pastoral)));

        assertThat(r.getStatus()).isEqualTo(StatusReserva.FINALIZADA);
    }

    @Test
    @DisplayName("Terceiro sem alçada não finaliza o uso")
    void terceiroNaoFinaliza() {
        Reserva r = localEmUso(UUID.randomUUID(), UUID.randomUUID());
        when(reservaRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> useCase().executar(r.getId().value(),
                new UsuarioActor(UUID.randomUUID(), "x", List.of(), List.of(), List.of(r.getPastoralMovimentoId()))))
                .isInstanceOf(ReservaNaoAutorizadaException.class);
        verify(reservaRepository, never()).save(any());
        assertThat(r.getStatus()).isEqualTo(StatusReserva.CONFIRMADA);
    }

    @Test
    @DisplayName("Reserva inexistente resulta em ReservaNaoEncontradaException")
    void reservaInexistente() {
        UUID id = UUID.randomUUID();
        when(reservaRepository.findById(new ReservaId(id))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().executar(id,
                new UsuarioActor(UUID.randomUUID(), "x", List.of(), List.of(), List.of())))
                .isInstanceOf(ReservaNaoEncontradaException.class);
    }
}
