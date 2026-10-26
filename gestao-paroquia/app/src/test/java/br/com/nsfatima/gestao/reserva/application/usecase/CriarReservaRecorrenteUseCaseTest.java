package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService;
import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService.PedidoReserva;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase.ComandoCriarRecorrente;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase.OcorrenciaHorario;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase.ResultadoRecorrencia;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException.ConflitoDetalhe;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarReservaRecorrenteUseCaseTest {

    @Mock private ReservaCriacaoService criacaoService;
    @Mock private ReservaAuthorizationService authorizationService;

    private CriarReservaRecorrenteUseCase useCase() {
        return new CriarReservaRecorrenteUseCase(criacaoService, authorizationService);
    }

    private UsuarioActor membroDaOrg(UUID orgId) {
        return new UsuarioActor(UUID.randomUUID(), "solicitante", List.of("ROLE_FIEL"), List.of(), List.of(orgId));
    }

    private OcorrenciaHorario ocorrencia(int diasAFrente) {
        Instant inicio = Instant.now().plus(diasAFrente, ChronoUnit.DAYS);
        return new OcorrenciaHorario(inicio, inicio.plus(1, ChronoUnit.HOURS));
    }

    private ConflitoDetalhe conflito(UUID itemId) {
        return new ConflitoDetalhe(itemId, "Local", TipoItemReserva.LOCAL, Instant.now(), Instant.now(),
                UUID.randomUUID(), "Responsável", "outra");
    }

    @Test
    @DisplayName("Deve lançar ReservaNaoAutorizadaException quando solicitante não é membro da pastoral")
    void deveLancarNaoAutorizadaQuandoNaoMembro() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = new UsuarioActor(
                UUID.randomUUID(), "x", List.of("ROLE_FIEL"), List.of(), List.of());
        ComandoCriarRecorrente comando = new ComandoCriarRecorrente(
                TipoItemReserva.LOCAL, UUID.randomUUID(), "Local", 15, "desc", orgId,
                List.of(ocorrencia(1)), false);

        assertThatThrownBy(() -> useCase().executar(comando, solicitante))
                .isInstanceOf(ReservaNaoAutorizadaException.class);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando ocorrências estão vazias")
    void deveLancarQuandoOcorrenciasVazias() {
        UUID orgId = UUID.randomUUID();
        ComandoCriarRecorrente comando = new ComandoCriarRecorrente(
                TipoItemReserva.LOCAL, UUID.randomUUID(), "Local", 15, "desc", orgId, List.of(), false);

        assertThatThrownBy(() -> useCase().executar(comando, membroDaOrg(orgId)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve registrar todas as ocorrências, no mesmo grupo e com o status da alçada, quando não há conflitos")
    void deveCriarReservasSemConflitos() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = membroDaOrg(orgId);
        ComandoCriarRecorrente comando = new ComandoCriarRecorrente(
                TipoItemReserva.LOCAL, UUID.randomUUID(), "Local", null, "desc", orgId,
                List.of(ocorrencia(1), ocorrencia(8)), false);

        when(criacaoService.conflitosDeAgenda(any())).thenReturn(List.of());
        when(criacaoService.janelaDeManutencao(any())).thenReturn(Optional.empty());
        when(authorizationService.determinarStatusInicial(solicitante, orgId)).thenReturn(StatusReserva.PENDENTE);
        when(criacaoService.registrar(any())).thenReturn(mock(Reserva.class));

        ResultadoRecorrencia resultado = useCase().executar(comando, solicitante);

        assertThat(resultado.reservasCriadas()).hasSize(2);
        assertThat(resultado.conflitosIdentificados()).isEmpty();
        ArgumentCaptor<PedidoReserva> pedidos = ArgumentCaptor.forClass(PedidoReserva.class);
        verify(criacaoService, times(2)).registrar(pedidos.capture());
        assertThat(pedidos.getAllValues()).allSatisfy(p -> {
            assertThat(p.grupoRecorrenciaId()).isEqualTo(resultado.grupoRecorrenciaId());
            assertThat(p.statusInicial()).isEqualTo(StatusReserva.PENDENTE);
            assertThat(p.eventoId()).isNull();
        });
    }

    @Test
    @DisplayName("Deve lançar ReservaConflitoException quando há conflitos e salvarValidasDescartarConflitos é falso")
    void deveLancarConflitoQuandoNaoDescartaConflitos() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = membroDaOrg(orgId);
        ComandoCriarRecorrente comando = new ComandoCriarRecorrente(
                TipoItemReserva.LOCAL, UUID.randomUUID(), "Local", 15, "desc", orgId,
                List.of(ocorrencia(1)), false);

        when(criacaoService.conflitosDeAgenda(any())).thenReturn(List.of(conflito(comando.itemId())));
        when(criacaoService.janelaDeManutencao(any())).thenReturn(Optional.empty());
        when(authorizationService.determinarStatusInicial(solicitante, orgId)).thenReturn(StatusReserva.PENDENTE);

        assertThatThrownBy(() -> useCase().executar(comando, solicitante))
                .isInstanceOf(ReservaConflitoException.class);
        verify(criacaoService, never()).registrar(any());
    }

    @Test
    @DisplayName("Ocorrência em período de manutenção do ativo conta como impedimento da série")
    void manutencaoImpedeOcorrencia() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = membroDaOrg(orgId);
        ComandoCriarRecorrente comando = new ComandoCriarRecorrente(
                TipoItemReserva.ATIVO, UUID.randomUUID(), "Projetor", 15, "desc", orgId,
                List.of(ocorrencia(1)), true);

        when(criacaoService.conflitosDeAgenda(any())).thenReturn(List.of());
        when(criacaoService.janelaDeManutencao(any())).thenReturn(Optional.of(conflito(comando.itemId())));
        when(authorizationService.determinarStatusInicial(solicitante, orgId)).thenReturn(StatusReserva.CONFIRMADA);

        ResultadoRecorrencia resultado = useCase().executar(comando, solicitante);

        assertThat(resultado.reservasCriadas()).isEmpty();
        assertThat(resultado.conflitosIdentificados()).hasSize(1);
    }

    @Test
    @DisplayName("Deve descartar ocorrências conflitantes e salvar apenas as válidas quando flag ativada")
    void deveDescartarConflitosQuandoFlagAtiva() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = membroDaOrg(orgId);
        UUID itemId = UUID.randomUUID();
        ComandoCriarRecorrente comando = new ComandoCriarRecorrente(
                TipoItemReserva.LOCAL, itemId, null, 15, "desc", orgId,
                List.of(ocorrencia(1), ocorrencia(8)), true);

        when(criacaoService.conflitosDeAgenda(any()))
                .thenReturn(List.of(conflito(itemId)))
                .thenReturn(List.of());
        when(criacaoService.janelaDeManutencao(any())).thenReturn(Optional.empty());
        when(authorizationService.determinarStatusInicial(solicitante, orgId)).thenReturn(StatusReserva.CONFIRMADA);
        when(criacaoService.registrar(any())).thenReturn(mock(Reserva.class));

        ResultadoRecorrencia resultado = useCase().executar(comando, solicitante);

        assertThat(resultado.reservasCriadas()).hasSize(1);
        assertThat(resultado.conflitosIdentificados()).hasSize(1);
    }
}
