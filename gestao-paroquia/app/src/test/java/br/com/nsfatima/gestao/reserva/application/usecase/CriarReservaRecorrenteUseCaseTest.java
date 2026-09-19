package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase.ComandoCriarRecorrente;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase.OcorrenciaHorario;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase.ResultadoRecorrencia;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
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
class CriarReservaRecorrenteUseCaseTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaConflitoService conflitoService;
    @Mock private ReservaAuthorizationService authorizationService;

    private CriarReservaRecorrenteUseCase useCase() {
        return new CriarReservaRecorrenteUseCase(reservaRepository, conflitoService, authorizationService);
    }

    private UsuarioActor membroDaOrg(UUID orgId) {
        return new UsuarioActor(UUID.randomUUID(), "solicitante", List.of("ROLE_FIEL"), List.of(), List.of(orgId));
    }

    private OcorrenciaHorario ocorrencia() {
        Instant inicio = Instant.now().plus(1, ChronoUnit.DAYS);
        return new OcorrenciaHorario(inicio, inicio.plus(1, ChronoUnit.HOURS));
    }

    @Test
    @DisplayName("Deve lançar ReservaNaoAutorizadaException quando solicitante não é membro da pastoral")
    void deveLancarNaoAutorizadaQuandoNaoMembro() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = new UsuarioActor(
                UUID.randomUUID(), "x", List.of("ROLE_FIEL"), List.of(), List.of());
        ComandoCriarRecorrente comando = new ComandoCriarRecorrente(
                TipoItemReserva.LOCAL, UUID.randomUUID(), "Local", 15, "desc", orgId,
                List.of(ocorrencia()), false);

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
    @DisplayName("Deve criar todas as reservas quando não há conflitos")
    void deveCriarReservasSemConflitos() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = membroDaOrg(orgId);
        ComandoCriarRecorrente comando = new ComandoCriarRecorrente(
                TipoItemReserva.LOCAL, UUID.randomUUID(), "Local", null, "desc", orgId,
                List.of(ocorrencia(), ocorrencia()), false);

        when(conflitoService.buscarColisoesComLock(any(), any(), any(), any())).thenReturn(List.of());
        when(authorizationService.determinarStatusInicial(solicitante, orgId)).thenReturn(StatusReserva.PENDENTE);
        when(reservaRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        ResultadoRecorrencia resultado = useCase().executar(comando, solicitante);

        assertThat(resultado.reservasCriadas()).hasSize(2);
        assertThat(resultado.conflitosIdentificados()).isEmpty();
        assertThat(resultado.grupoRecorrenciaId()).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar ReservaConflitoException quando há conflitos e salvarValidasDescartarConflitos é falso")
    void deveLancarConflitoQuandoNaoDescartaConflitos() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = membroDaOrg(orgId);
        ComandoCriarRecorrente comando = new ComandoCriarRecorrente(
                TipoItemReserva.LOCAL, UUID.randomUUID(), "Local", 15, "desc", orgId,
                List.of(ocorrencia()), false);

        Instant inicioChoque = Instant.now().plus(1, ChronoUnit.DAYS);
        IntervaloReserva intervaloChoque =
                new IntervaloReserva(inicioChoque, inicioChoque.plus(1, ChronoUnit.HOURS), 0);
        Reserva choque = Reserva.criar(ReservaId.gerar(), TipoItemReserva.LOCAL, comando.itemId(), intervaloChoque,
                "outra", UUID.randomUUID(), orgId, null, null, StatusReserva.CONFIRMADA, Instant.now());

        when(conflitoService.buscarColisoesComLock(any(), any(), any(), any())).thenReturn(List.of(choque));
        when(authorizationService.determinarStatusInicial(solicitante, orgId)).thenReturn(StatusReserva.PENDENTE);

        assertThatThrownBy(() -> useCase().executar(comando, solicitante))
                .isInstanceOf(ReservaConflitoException.class);
        verify(reservaRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Deve descartar ocorrências conflitantes e salvar apenas as válidas quando flag ativada")
    void deveDescartarConflitosQuandoFlagAtiva() {
        UUID orgId = UUID.randomUUID();
        UsuarioActor solicitante = membroDaOrg(orgId);
        UUID itemId = UUID.randomUUID();
        ComandoCriarRecorrente comando = new ComandoCriarRecorrente(
                TipoItemReserva.LOCAL, itemId, null, 15, "desc", orgId,
                List.of(ocorrencia(), ocorrencia()), true);

        Instant inicioChoque = Instant.now().plus(1, ChronoUnit.DAYS);
        IntervaloReserva intervaloChoque =
                new IntervaloReserva(inicioChoque, inicioChoque.plus(1, ChronoUnit.HOURS), 0);
        Reserva choque = Reserva.criar(ReservaId.gerar(), TipoItemReserva.LOCAL, itemId, intervaloChoque,
                "outra", UUID.randomUUID(), orgId, null, null, StatusReserva.CONFIRMADA, Instant.now());

        when(conflitoService.buscarColisoesComLock(any(), any(), any(), any()))
                .thenReturn(List.of(choque))
                .thenReturn(List.of());
        when(authorizationService.determinarStatusInicial(solicitante, orgId)).thenReturn(StatusReserva.CONFIRMADA);
        when(reservaRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        ResultadoRecorrencia resultado = useCase().executar(comando, solicitante);

        assertThat(resultado.reservasCriadas()).hasSize(1);
        assertThat(resultado.conflitosIdentificados()).hasSize(1);
    }
}
