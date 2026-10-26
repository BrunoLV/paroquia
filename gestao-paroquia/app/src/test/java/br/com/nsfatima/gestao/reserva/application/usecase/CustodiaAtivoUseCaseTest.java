package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
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
class CustodiaAtivoUseCaseTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private PublicadorReservaDeAtivo publicador;

    private final ReservaAuthorizationService authorization =
            new ReservaAuthorizationService(new PadraoReservaAutorizacaoPolicy());

    private Reserva reserva(UUID solicitante, UUID pastoral) {
        Instant inicio = Instant.now().plus(Duration.ofHours(1));
        return Reserva.criar(ReservaId.gerar(), TipoItemReserva.ATIVO, UUID.randomUUID(),
                new IntervaloReserva(inicio, inicio.plus(Duration.ofHours(2)), 0), "d", solicitante, pastoral,
                null, null, StatusReserva.CONFIRMADA, Instant.now());
    }

    private UsuarioActor ator(UUID id, List<String> roles, List<UUID> coordena, List<UUID> membro) {
        return new UsuarioActor(id, "x", roles, coordena, membro);
    }

    private RegistrarRetiradaAtivoUseCase retirada() {
        return new RegistrarRetiradaAtivoUseCase(reservaRepository, authorization, publicador);
    }

    private RegistrarDevolucaoAtivoUseCase devolucao() {
        return new RegistrarDevolucaoAtivoUseCase(reservaRepository, authorization, publicador);
    }

    @Test
    @DisplayName("Solicitante, coordenador e administração registram a retirada")
    void retiradaPermitida() {
        UUID solicitante = UUID.randomUUID();
        UUID pastoral = UUID.randomUUID();
        List<UsuarioActor> autorizados = List.of(
                ator(solicitante, List.of(), List.of(), List.of()),
                ator(UUID.randomUUID(), List.of(), List.of(pastoral), List.of(pastoral)),
                ator(UUID.randomUUID(), List.of("ROLE_PAROCO"), List.of(), List.of()));
        for (UsuarioActor ator : autorizados) {
            Reserva r = reserva(solicitante, pastoral);
            when(reservaRepository.findById(r.getId())).thenReturn(Optional.of(r));
            when(reservaRepository.save(r)).thenReturn(r);

            assertThat(retirada().executar(r.getId().value(), ator).getStatus()).isEqualTo(StatusReserva.RETIRADO);
        }
    }

    @Test
    @DisplayName("Terceiro, mesmo membro da pastoral, não registra retirada nem devolução")
    void terceiroNegado() {
        UUID pastoral = UUID.randomUUID();
        UsuarioActor terceiro = ator(UUID.randomUUID(), List.of("ROLE_FIEL"), List.of(), List.of(pastoral));
        Reserva r = reserva(UUID.randomUUID(), pastoral);
        when(reservaRepository.findById(r.getId())).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> retirada().executar(r.getId().value(), terceiro))
                .isInstanceOf(ReservaNaoAutorizadaException.class);
        assertThatThrownBy(() -> devolucao().executar(r.getId().value(), "ok", terceiro))
                .isInstanceOf(ReservaNaoAutorizadaException.class);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("O solicitante registra a devolução e o patrimônio é notificado")
    void devolucaoPermitidaNotifica() {
        UUID solicitante = UUID.randomUUID();
        Reserva r = reserva(solicitante, UUID.randomUUID());
        r.registrarRetirada(solicitante, Instant.now());
        when(reservaRepository.findById(r.getId())).thenReturn(Optional.of(r));
        when(reservaRepository.save(r)).thenReturn(r);

        Reserva resultado = devolucao().executar(r.getId().value(), "sem avarias",
                ator(solicitante, List.of(), List.of(), List.of()));

        assertThat(resultado.getStatus()).isEqualTo(StatusReserva.FINALIZADA);
        verify(publicador).publicarSeForAtivo(r);
    }
}
