package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService;
import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService.PedidoReserva;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaUseCase.ComandoCriarReserva;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarReservaUseCaseTest {

    @Mock private ReservaCriacaoService criacaoService;
    @Mock private ReservaAuthorizationService authorizationService;

    private CriarReservaUseCase useCase() {
        return new CriarReservaUseCase(criacaoService, authorizationService);
    }

    private ComandoCriarReserva comando(TipoItemReserva tipo, UUID orgId, Integer buffer) {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        return new ComandoCriarReserva(tipo, UUID.randomUUID(), "Item", inicio, inicio.plus(1, ChronoUnit.HOURS),
                buffer, "desc", orgId);
    }

    private UsuarioActor membro(UUID orgId) {
        return new UsuarioActor(UUID.randomUUID(), "x", List.of(), List.of(), List.of(orgId));
    }

    @Test
    @DisplayName("Deve lançar ReservaNaoAutorizadaException quando solicitante não é membro da pastoral")
    void deveLancarNaoAutorizadaQuandoNaoMembro() {
        UsuarioActor solicitante = new UsuarioActor(UUID.randomUUID(), "x", List.of(), List.of(), List.of());

        assertThatThrownBy(() -> useCase().executar(comando(TipoItemReserva.LOCAL, UUID.randomUUID(), null),
                solicitante)).isInstanceOf(ReservaNaoAutorizadaException.class);
        verifyNoInteractions(criacaoService);
    }

    @Test
    @DisplayName("Status inicial vem só da alçada do solicitante e a reserva nunca nasce vinculada a evento ou série")
    void statusInicialVemDaAlcadaSemVinculos() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = membro(orgId);
        Reserva criada = mock(Reserva.class);
        when(authorizationService.determinarStatusInicial(solicitante, orgId)).thenReturn(StatusReserva.PENDENTE);
        when(criacaoService.criar(any())).thenReturn(criada);

        Reserva resultado = useCase().executar(comando(TipoItemReserva.ATIVO, orgId, null), solicitante);

        assertThat(resultado).isSameAs(criada);
        ArgumentCaptor<PedidoReserva> pedido = ArgumentCaptor.forClass(PedidoReserva.class);
        verify(criacaoService).criar(pedido.capture());
        assertThat(pedido.getValue().statusInicial()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(pedido.getValue().eventoId()).isNull();
        assertThat(pedido.getValue().grupoRecorrenciaId()).isNull();
        assertThat(pedido.getValue().solicitanteId()).isEqualTo(solicitante.id());
    }

    @Test
    @DisplayName("Deve usar buffer padrão quando bufferMinutos não é informado")
    void deveUsarBufferPadraoQuandoNaoInformado() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = membro(orgId);
        when(authorizationService.determinarStatusInicial(solicitante, orgId)).thenReturn(StatusReserva.CONFIRMADA);

        useCase().executar(comando(TipoItemReserva.LOCAL, orgId, null), solicitante);

        ArgumentCaptor<PedidoReserva> pedido = ArgumentCaptor.forClass(PedidoReserva.class);
        verify(criacaoService).criar(pedido.capture());
        assertThat(pedido.getValue().intervalo().bufferMinutos()).isEqualTo(30);
        verify(criacaoService, never()).criarTodas(any());
    }
}
