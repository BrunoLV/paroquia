package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpirarReservaPendenteUseCaseTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private PublicadorReservaDeAtivo publicador;

    private Reserva reserva(StatusReserva status, Instant inicio) {
        return Reserva.criar(ReservaId.gerar(), TipoItemReserva.ATIVO, UUID.randomUUID(),
                new IntervaloReserva(inicio, inicio.plus(Duration.ofHours(1)), 0), "d", UUID.randomUUID(),
                UUID.randomUUID(), null, null, status, inicio.minus(Duration.ofDays(10)));
    }

    @Test
    @DisplayName("Expira a reserva pendente de prazo vencido e notifica o patrimônio")
    void expiraPendenteVencida() {
        Instant agora = Instant.now();
        Reserva r = reserva(StatusReserva.PENDENTE, agora.plus(Duration.ofHours(10)));
        when(reservaRepository.findById(r.getId())).thenReturn(Optional.of(r));
        when(reservaRepository.save(r)).thenReturn(r);

        Optional<Reserva> resultado = new ExpirarReservaPendenteUseCase(reservaRepository, publicador)
                .executar(r.getId().value(), agora);

        assertThat(resultado).containsSame(r);
        assertThat(r.getStatus()).isEqualTo(StatusReserva.EXPIRADA);
        verify(publicador).publicarSeForAtivo(r);
    }

    @Test
    @DisplayName("Reserva analisada entre a varredura e a expiração é preservada")
    void preservaReservaJaAnalisada() {
        Instant agora = Instant.now();
        Reserva r = reserva(StatusReserva.CONFIRMADA, agora.plus(Duration.ofHours(10)));
        when(reservaRepository.findById(r.getId())).thenReturn(Optional.of(r));

        Optional<Reserva> resultado = new ExpirarReservaPendenteUseCase(reservaRepository, publicador)
                .executar(r.getId().value(), agora);

        assertThat(resultado).isEmpty();
        verify(reservaRepository, never()).save(any());
    }
}
