package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarNotificacaoUseCase;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpirarReservasPendentesUseCaseTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ExpirarReservaPendenteUseCase expirarUma;
    @Mock private GerenciarNotificacaoUseCase notificacaoUseCase;

    private ExpirarReservasPendentesUseCase useCase() {
        return new ExpirarReservasPendentesUseCase(reservaRepository, expirarUma, notificacaoUseCase);
    }

    private Reserva pendente(Instant criadaEm, Instant inicio) {
        return Reserva.criar(ReservaId.gerar(), TipoItemReserva.LOCAL, UUID.randomUUID(),
                new IntervaloReserva(inicio, inicio.plus(Duration.ofHours(1)), 0), "d", UUID.randomUUID(),
                UUID.randomUUID(), null, null, StatusReserva.PENDENTE, criadaEm);
    }

    @Test
    @DisplayName("Não expira pedido recém-criado com menos de 48h de antecedência: só vence no início")
    void naoExpiraPedidoTardioRecemCriado() {
        Instant agora = Instant.now();
        Reserva tardio = pendente(agora.minus(Duration.ofMinutes(5)), agora.plus(Duration.ofHours(10)));
        when(reservaRepository.findPendentesExpiracao(any())).thenReturn(List.of(tardio));

        int expiradas = useCase().executar();

        assertThat(expiradas).isZero();
        verify(expirarUma, never()).executar(any(), any());
    }

    @Test
    @DisplayName("Expira pedido cujo prazo de análise venceu, cada um em sua própria transação")
    void expiraPedidosVencidosUmAUm() {
        Instant agora = Instant.now();
        Reserva a = pendente(agora.minus(Duration.ofDays(5)), agora.plus(Duration.ofHours(20)));
        Reserva b = pendente(agora.minus(Duration.ofDays(5)), agora.plus(Duration.ofHours(30)));
        when(reservaRepository.findPendentesExpiracao(any())).thenReturn(List.of(a, b));
        when(expirarUma.executar(eq(a.getId().value()), any())).thenReturn(Optional.of(a));
        when(expirarUma.executar(eq(b.getId().value()), any())).thenReturn(Optional.of(b));

        int expiradas = useCase().executar();

        assertThat(expiradas).isEqualTo(2);
        verify(notificacaoUseCase).publicar(eq(a.getSolicitanteId()), anyString());
        verify(notificacaoUseCase).publicar(eq(b.getSolicitanteId()), anyString());
    }

    @Test
    @DisplayName("Falha ao expirar ou notificar uma reserva não impede as demais")
    void falhaIsoladaPorReserva() {
        Instant agora = Instant.now();
        Reserva a = pendente(agora.minus(Duration.ofDays(5)), agora.plus(Duration.ofHours(20)));
        Reserva b = pendente(agora.minus(Duration.ofDays(5)), agora.plus(Duration.ofHours(30)));
        when(reservaRepository.findPendentesExpiracao(any())).thenReturn(List.of(a, b));
        when(expirarUma.executar(eq(a.getId().value()), any())).thenThrow(new IllegalStateException("falha"));
        when(expirarUma.executar(eq(b.getId().value()), any())).thenReturn(Optional.of(b));
        doThrow(new IllegalStateException("sem notificação")).when(notificacaoUseCase).publicar(any(), anyString());

        int expiradas = useCase().executar();

        assertThat(expiradas).isEqualTo(1);
        verify(notificacaoUseCase).publicar(eq(b.getSolicitanteId()), anyString());
    }
}
