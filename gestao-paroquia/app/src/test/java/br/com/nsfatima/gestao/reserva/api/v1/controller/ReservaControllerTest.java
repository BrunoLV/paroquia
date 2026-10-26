package br.com.nsfatima.gestao.reserva.api.v1.controller;

import br.com.nsfatima.gestao.reserva.api.v1.dto.CancelarReservaRequest;
import br.com.nsfatima.gestao.reserva.api.v1.dto.CriarReservaRecorrenteRequest;
import br.com.nsfatima.gestao.reserva.api.v1.dto.CriarReservaRequest;
import br.com.nsfatima.gestao.reserva.api.v1.dto.DisponibilidadeFaixaResponse;
import br.com.nsfatima.gestao.reserva.api.v1.dto.MetricasReservasResponse;
import br.com.nsfatima.gestao.reserva.api.v1.dto.RegistrarDevolucaoRequest;
import br.com.nsfatima.gestao.reserva.api.v1.dto.RejeitarReservaRequest;
import br.com.nsfatima.gestao.reserva.api.v1.dto.ReservaRecorrenteResponse;
import br.com.nsfatima.gestao.reserva.api.v1.dto.ReservaResponse;
import br.com.nsfatima.gestao.reserva.application.dto.DisponibilidadeFaixaDto;
import br.com.nsfatima.gestao.reserva.application.dto.MetricasReservasDto;
import br.com.nsfatima.gestao.reserva.application.dto.ReservaDetalheDto;
import br.com.nsfatima.gestao.reserva.application.usecase.AprovarReservaUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.BuscarReservaPorIdUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.CancelarReservaUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase.OcorrenciaHorario;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaRecorrenteUseCase.ResultadoRecorrencia;
import br.com.nsfatima.gestao.reserva.application.usecase.CriarReservaUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.FinalizarUsoLocalUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.ListarReservasUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.ObterDisponibilidadeItemUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.ObterMetricasReservasUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.RegistrarDevolucaoAtivoUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.RegistrarRetiradaAtivoUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.RejeitarReservaUseCase;
import br.com.nsfatima.gestao.reserva.application.usecase.EnriquecedorReservaService;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaFiltro;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import br.com.nsfatima.gestao.reserva.infrastructure.security.ReservaSecurityAdapter;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    @Mock private CriarReservaUseCase criarReservaUseCase;
    @Mock private CriarReservaRecorrenteUseCase criarReservaRecorrenteUseCase;
    @Mock private AprovarReservaUseCase aprovarReservaUseCase;
    @Mock private RejeitarReservaUseCase rejeitarReservaUseCase;
    @Mock private CancelarReservaUseCase cancelarReservaUseCase;
    @Mock private RegistrarRetiradaAtivoUseCase registrarRetiradaAtivoUseCase;
    @Mock private RegistrarDevolucaoAtivoUseCase registrarDevolucaoAtivoUseCase;
    @Mock private ListarReservasUseCase listarReservasUseCase;
    @Mock private BuscarReservaPorIdUseCase buscarReservaPorIdUseCase;
    @Mock private ObterDisponibilidadeItemUseCase obterDisponibilidadeItemUseCase;
    @Mock private ObterMetricasReservasUseCase obterMetricasReservasUseCase;
    @Mock private EnriquecedorReservaService enriquecedorReservaService;
    @Mock private FinalizarUsoLocalUseCase finalizarUsoLocalUseCase;
    @Mock private ReservaSecurityAdapter securityAdapter;

    private ReservaController controller() {
        return new ReservaController(
                criarReservaUseCase, criarReservaRecorrenteUseCase, aprovarReservaUseCase, rejeitarReservaUseCase,
                cancelarReservaUseCase, registrarRetiradaAtivoUseCase, registrarDevolucaoAtivoUseCase,
                finalizarUsoLocalUseCase, listarReservasUseCase, buscarReservaPorIdUseCase,
                obterDisponibilidadeItemUseCase, obterMetricasReservasUseCase, enriquecedorReservaService,
                securityAdapter);
    }

    private UsuarioActor ator() {
        return new UsuarioActor(UUID.randomUUID(), "ator", List.of("ROLE_ADMIN"), List.of(), List.of());
    }

    private Reserva reserva() {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 0);
        return Reserva.criar(ReservaId.gerar(), TipoItemReserva.LOCAL, UUID.randomUUID(), intervalo, "desc",
                UUID.randomUUID(), UUID.randomUUID(), null, null, StatusReserva.PENDENTE, Instant.now());
    }

    private ReservaDetalheDto dtoDe(Reserva r) {
        return new ReservaDetalheDto(r.getId().value(), r.getTipoItem(), r.getItemId(), "item",
                r.getIntervalo().dataHoraInicio(), r.getIntervalo().dataHoraFim(), r.getIntervalo().bufferMinutos(),
                r.getIntervalo().fimComBuffer(), r.getDescricao(), r.getSolicitanteId(), "solicitante",
                r.getPastoralMovimentoId(), "pastoral", r.getEventoId(), null, r.getGrupoRecorrenciaId(),
                r.getStatus(), r.getAprovadorId(), null, r.getAprovadoEm(), r.getMotivoCancelamento(),
                r.getCanceladoPorId(), null, r.getCheckAvarias(), r.getCreatedAt(), r.getUpdatedAt());
    }

    @Test
    @DisplayName("criar delega ao use case, enriquece e converte para ReservaResponse")
    void deveCriarReserva() {
        UsuarioActor ator = ator();
        when(securityAdapter.obterAtorAutenticado()).thenReturn(ator);
        Reserva criada = reserva();
        when(criarReservaUseCase.executar(any(), eq(ator))).thenReturn(criada);
        ReservaDetalheDto dto = dtoDe(criada);
        when(enriquecedorReservaService.enriquecer(criada)).thenReturn(dto);

        CriarReservaRequest request = new CriarReservaRequest(
                TipoItemReserva.LOCAL, UUID.randomUUID(), "Item", Instant.now().plus(1, ChronoUnit.HOURS),
                Instant.now().plus(2, ChronoUnit.HOURS), 15, "desc", UUID.randomUUID());

        ReservaResponse response = controller().criar(request);

        assertThat(response.id()).isEqualTo(criada.getId().value());
    }

    @Test
    @DisplayName("criarRecorrente delega, enriquece a lista e mapeia conflitos")
    void deveCriarReservaRecorrente() {
        UsuarioActor ator = ator();
        when(securityAdapter.obterAtorAutenticado()).thenReturn(ator);
        Reserva criada = reserva();
        UUID grupoId = UUID.randomUUID();
        ResultadoRecorrencia resultado = new ResultadoRecorrencia(grupoId, List.of(criada), List.of());
        when(criarReservaRecorrenteUseCase.executar(any(), eq(ator))).thenReturn(resultado);
        when(enriquecedorReservaService.enriquecer(List.of(criada))).thenReturn(List.of(dtoDe(criada)));

        CriarReservaRecorrenteRequest request = new CriarReservaRecorrenteRequest(
                TipoItemReserva.LOCAL, UUID.randomUUID(), "Item", 15, "desc", UUID.randomUUID(),
                List.of(new OcorrenciaHorario(Instant.now(), Instant.now().plusSeconds(3600))), false);

        ReservaRecorrenteResponse response = controller().criarRecorrente(request);

        assertThat(response.grupoRecorrenciaId()).isEqualTo(grupoId);
        assertThat(response.reservasCriadas()).hasSize(1);
        assertThat(response.conflitosDescartados()).isEmpty();
    }

    @Test
    @DisplayName("listar monta filtro a partir dos parâmetros e mapeia a resposta")
    void deveListarReservas() {
        Reserva r = reserva();
        when(listarReservasUseCase.executar(any(ReservaFiltro.class))).thenReturn(List.of(dtoDe(r)));

        List<ReservaResponse> resultado = controller().listar(
                TipoItemReserva.LOCAL, UUID.randomUUID(), StatusReserva.PENDENTE, UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(3600), "termo");

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("buscarPorId delega ao use case e converte dto para response")
    void deveBuscarPorId() {
        Reserva r = reserva();
        UUID id = r.getId().value();
        when(buscarReservaPorIdUseCase.executar(id)).thenReturn(dtoDe(r));

        ReservaResponse response = controller().buscarPorId(id);

        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("aprovar delega ao use case, enriquece e retorna response")
    void deveAprovar() {
        UsuarioActor ator = ator();
        when(securityAdapter.obterAtorAutenticado()).thenReturn(ator);
        Reserva r = reserva();
        UUID id = r.getId().value();
        when(aprovarReservaUseCase.executar(id, ator)).thenReturn(r);
        when(enriquecedorReservaService.enriquecer(r)).thenReturn(dtoDe(r));

        ReservaResponse response = controller().aprovar(id);

        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("rejeitar delega motivo ao use case")
    void deveRejeitar() {
        UsuarioActor ator = ator();
        when(securityAdapter.obterAtorAutenticado()).thenReturn(ator);
        Reserva r = reserva();
        UUID id = r.getId().value();
        when(rejeitarReservaUseCase.executar(id, "sem vaga", ator)).thenReturn(r);
        when(enriquecedorReservaService.enriquecer(r)).thenReturn(dtoDe(r));

        ReservaResponse response = controller().rejeitar(id, new RejeitarReservaRequest("sem vaga"));

        assertThat(response.id()).isEqualTo(id);
        verify(rejeitarReservaUseCase).executar(id, "sem vaga", ator);
    }

    @Test
    @DisplayName("cancelar delega motivo e itemNome ao use case")
    void deveCancelar() {
        UsuarioActor ator = ator();
        when(securityAdapter.obterAtorAutenticado()).thenReturn(ator);
        Reserva r = reserva();
        UUID id = r.getId().value();
        when(cancelarReservaUseCase.executar(id, "motivo", "Salão", ator)).thenReturn(r);
        when(enriquecedorReservaService.enriquecer(r)).thenReturn(dtoDe(r));

        ReservaResponse response = controller().cancelar(id, new CancelarReservaRequest("motivo", "Salão"));

        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("registrarRetirada delega ao use case")
    void deveRegistrarRetirada() {
        UsuarioActor ator = ator();
        when(securityAdapter.obterAtorAutenticado()).thenReturn(ator);
        Reserva r = reserva();
        UUID id = r.getId().value();
        when(registrarRetiradaAtivoUseCase.executar(id, ator)).thenReturn(r);
        when(enriquecedorReservaService.enriquecer(r)).thenReturn(dtoDe(r));

        ReservaResponse response = controller().registrarRetirada(id);

        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("registrarDevolucao delega checkAvarias ao use case")
    void deveRegistrarDevolucao() {
        UsuarioActor ator = ator();
        when(securityAdapter.obterAtorAutenticado()).thenReturn(ator);
        Reserva r = reserva();
        UUID id = r.getId().value();
        when(registrarDevolucaoAtivoUseCase.executar(id, "sem avarias", ator)).thenReturn(r);
        when(enriquecedorReservaService.enriquecer(r)).thenReturn(dtoDe(r));

        ReservaResponse response = controller().registrarDevolucao(id, new RegistrarDevolucaoRequest("sem avarias"));

        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("finalizarUso delega ao use case e converte para ReservaResponse")
    void deveFinalizarUsoDeLocal() {
        UsuarioActor ator = ator();
        when(securityAdapter.obterAtorAutenticado()).thenReturn(ator);
        Reserva r = reserva();
        UUID id = r.getId().value();
        when(finalizarUsoLocalUseCase.executar(id, ator)).thenReturn(r);
        when(enriquecedorReservaService.enriquecer(r)).thenReturn(dtoDe(r));

        ReservaResponse response = controller().finalizarUso(id);

        assertThat(response.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("obterDisponibilidade delega ao use case e mapeia faixas")
    void deveObterDisponibilidade() {
        UUID itemId = UUID.randomUUID();
        DisponibilidadeFaixaDto faixa = new DisponibilidadeFaixaDto(
                UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(3600), Instant.now().plusSeconds(4500),
                15, StatusReserva.CONFIRMADA, "Reservado", "desc");
        when(obterDisponibilidadeItemUseCase.executar(TipoItemReserva.LOCAL, itemId, null))
                .thenReturn(List.of(faixa));

        List<DisponibilidadeFaixaResponse> resultado =
                controller().obterDisponibilidade(TipoItemReserva.LOCAL, itemId, null);

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("obterMetricas delega ao use case e converte dto")
    void deveObterMetricas() {
        UUID pastoralId = UUID.randomUUID();
        when(obterMetricasReservasUseCase.executar(pastoralId))
                .thenReturn(new MetricasReservasDto(1L, 2L, 3L, 4L));

        MetricasReservasResponse response = controller().obterMetricas(pastoralId);

        assertThat(response.pendentesAprovacao()).isEqualTo(1L);
        assertThat(response.totalMesAtual()).isEqualTo(4L);
    }
}
