package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.calendario.application.usecase.observacao.RegistrarObservacaoSistemaUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoEncontradaException;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarReservaUseCaseTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaAuthorizationService authorizationService;
    @Mock private RegistrarObservacaoSistemaUseCase registerSystemObservacaoUseCase;
    @Mock private PublicadorReservaDeAtivo publicadorReservaDeAtivo;

    private CancelarReservaUseCase useCase() {
        return new CancelarReservaUseCase(
                reservaRepository, authorizationService, registerSystemObservacaoUseCase, publicadorReservaDeAtivo);
    }

    private Reserva reserva(TipoItemReserva tipo, UUID eventoId) {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 0);
        return Reserva.criar(ReservaId.generate(), tipo, UUID.randomUUID(), intervalo, "desc",
                UUID.randomUUID(), UUID.randomUUID(), eventoId, null, StatusReserva.CONFIRMADA, Instant.now());
    }

    @Test
    @DisplayName("Deve lançar ReservaNaoEncontradaException quando reserva não existe")
    void deveLancarNotFoundQuandoReservaInexistente() {
        UUID id = UUID.randomUUID();
        when(reservaRepository.findById(new ReservaId(id))).thenReturn(Optional.empty());
        UsuarioActor operador = new UsuarioActor(UUID.randomUUID(), "op", List.of("ROLE_ADMIN"), List.of(), List.of());

        assertThatThrownBy(() -> useCase().executar(id, "motivo", null, operador))
                .isInstanceOf(ReservaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve cancelar reserva sem evento vinculado e não registrar observação")
    void deveCancelarSemEvento() {
        Reserva reserva = reserva(TipoItemReserva.LOCAL, null);
        UUID id = reserva.getId().value();
        when(reservaRepository.findById(new ReservaId(id))).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(reserva)).thenReturn(reserva);
        UsuarioActor operador = new UsuarioActor(
                UUID.randomUUID(), "admin", List.of("ROLE_ADMIN"), List.of(), List.of());

        Reserva resultado = useCase().executar(id, "Motivo do cancelamento com detalhes suficientes", null, operador);

        assertThat(resultado.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        verify(registerSystemObservacaoUseCase, never()).execute(any(), any(), any(), any(), any(), any());
        verify(publicadorReservaDeAtivo).publicarSeForAtivo(reserva);
    }

    @Test
    @DisplayName("Deve cancelar reserva de ativo vinculada a evento, publicar evento e registrar observação")
    void deveCancelarComEventoEAtivo() {
        UUID eventoId = UUID.randomUUID();
        Reserva reserva = reserva(TipoItemReserva.ATIVO, eventoId);
        UUID id = reserva.getId().value();
        when(reservaRepository.findById(new ReservaId(id))).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(reserva)).thenReturn(reserva);
        UsuarioActor operador = new UsuarioActor(
                UUID.randomUUID(), "coordenador", List.of("COORD_PASTORAL"), List.of(), List.of());

        useCase().executar(id, "Necessidade de reagendamento urgente", "Projetor XYZ", operador);

        verify(publicadorReservaDeAtivo).publicarSeForAtivo(reserva);
        verify(registerSystemObservacaoUseCase).execute(
                eq(eventoId), eq(TipoObservacaoInput.CANCELAMENTO), anyString(),
                eq(operador.id()), eq(operador.nome()), eq("cancelamento-avulso-reserva"));
    }

    @Test
    @DisplayName("Deve usar identificação padrão do recurso quando itemNome não é informado")
    void deveUsarNomeDefaultQuandoItemNomeAusente() {
        UUID eventoId = UUID.randomUUID();
        Reserva reserva = reserva(TipoItemReserva.ATIVO, eventoId);
        UUID id = reserva.getId().value();
        when(reservaRepository.findById(new ReservaId(id))).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(reserva)).thenReturn(reserva);
        UUID operadorId = UUID.randomUUID();
        UsuarioActor operador = new UsuarioActor(operadorId, null, List.of("ROLE_ADMIN"), List.of(), List.of());

        useCase().executar(id, "Motivo qualquer razoavelmente detalhado", "", operador);

        verify(registerSystemObservacaoUseCase).execute(
                eq(eventoId), eq(TipoObservacaoInput.CANCELAMENTO), anyString(), eq(operadorId), eq(null),
                eq("cancelamento-avulso-reserva"));
    }
}
