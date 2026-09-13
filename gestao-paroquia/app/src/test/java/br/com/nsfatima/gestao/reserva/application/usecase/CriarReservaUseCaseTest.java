package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.application.port.out.ConsultaManutencaoAtivoPort;
import br.com.nsfatima.gestao.reserva.application.port.out.ConsultaManutencaoAtivoPort.JanelaIndisponivel;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaUseCase.ComandoCriarReserva;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaOperacaoInvalidaException;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaConflitoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarReservaUseCaseTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaConflitoService conflitoService;
    @Mock private ReservaAuthorizationService authorizationService;
    @Mock private ConsultaManutencaoAtivoPort consultaManutencaoPort;
    @Mock private PublicadorReservaDeAtivo publicadorReservaDeAtivo;

    private CriarReservaUseCase useCase() {
        return new CriarReservaUseCase(
                reservaRepository, conflitoService, authorizationService, consultaManutencaoPort,
                publicadorReservaDeAtivo);
    }

    private ComandoCriarReserva comando(TipoItemReserva tipo, UUID orgId, UUID eventoId) {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        return new ComandoCriarReserva(tipo, UUID.randomUUID(), "Item", inicio, inicio.plus(1, ChronoUnit.HOURS),
                null, "desc", orgId, eventoId, null);
    }

    @Test
    @DisplayName("Deve lançar ReservaNaoAutorizadaException quando solicitante não é membro da pastoral")
    void deveLancarNaoAutorizadaQuandoNaoMembro() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = new UsuarioActor(UUID.randomUUID(), "x", List.of(), List.of(), List.of());

        assertThatThrownBy(() -> useCase().executar(comando(TipoItemReserva.LOCAL, orgId, null), solicitante))
                .isInstanceOf(ReservaNaoAutorizadaException.class);
        verify(conflitoService, never()).validarDisponibilidadeComLock(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar ReservaOperacaoInvalidaException quando ativo está em manutenção no período")
    void deveLancarQuandoAtivoEmManutencao() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = new UsuarioActor(UUID.randomUUID(), "x", List.of(), List.of(), List.of(orgId));
        ComandoCriarReserva cmd = comando(TipoItemReserva.ATIVO, orgId, null);

        when(consultaManutencaoPort.janelasDeManutencao(any(), any(), any()))
                .thenReturn(List.of(new JanelaIndisponivel(cmd.dataHoraInicio(), cmd.dataHoraFim())));

        assertThatThrownBy(() -> useCase().executar(cmd, solicitante))
                .isInstanceOf(ReservaOperacaoInvalidaException.class);
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar reserva CONFIRMADA e publicar evento de ativo quando vinculada a evento")
    void deveCriarReservaConfirmadaQuandoVinculadaAEvento() {
        UUID orgId = UUID.randomUUID();
        UUID eventoId = UUID.randomUUID();
        UsuarioActor solicitante = new UsuarioActor(UUID.randomUUID(), "x", List.of(), List.of(), List.of(orgId));
        ComandoCriarReserva cmd = comando(TipoItemReserva.ATIVO, orgId, eventoId);

        when(consultaManutencaoPort.janelasDeManutencao(any(), any(), any())).thenReturn(List.of());
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reserva resultado = useCase().executar(cmd, solicitante);

        assertThat(resultado.getStatus()).isEqualTo(StatusReserva.CONFIRMADA);
        verify(publicadorReservaDeAtivo).publicarSeForAtivo(resultado);
        verify(authorizationService, never()).determinarStatusInicial(any(), any());
    }

    @Test
    @DisplayName("Deve criar reserva de LOCAL usando status determinado pela política quando sem evento")
    void deveCriarReservaLocalComStatusDaPolitica() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = new UsuarioActor(UUID.randomUUID(), "x", List.of(), List.of(), List.of(orgId));
        ComandoCriarReserva cmd = comando(TipoItemReserva.LOCAL, orgId, null);

        when(authorizationService.determinarStatusInicial(solicitante, orgId)).thenReturn(StatusReserva.PENDENTE);
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reserva resultado = useCase().executar(cmd, solicitante);

        assertThat(resultado.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        verify(publicadorReservaDeAtivo, never()).publicarSeForAtivo(any());
        verify(consultaManutencaoPort, never()).janelasDeManutencao(any(), any(), any());
    }

    @Test
    @DisplayName("Deve usar buffer padrão quando bufferMinutos não é informado")
    void deveUsarBufferPadraoQuandoNaoInformado() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = new UsuarioActor(UUID.randomUUID(), "x", List.of(), List.of(), List.of(orgId));
        ComandoCriarReserva cmd = comando(TipoItemReserva.LOCAL, orgId, null);

        when(authorizationService.determinarStatusInicial(solicitante, orgId)).thenReturn(StatusReserva.CONFIRMADA);
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Reserva resultado = useCase().executar(cmd, solicitante);

        assertThat(resultado.getIntervalo().bufferMinutos()).isEqualTo(30);
    }
}
