package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.dto.MetricasReservasDto;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaFiltro;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObterMetricasReservasUseCaseTest {

    @Mock private ReservaRepository reservaRepository;

    private ObterMetricasReservasUseCase useCase() {
        return new ObterMetricasReservasUseCase(reservaRepository);
    }

    private Reserva reserva(StatusReserva status) {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 0);
        Reserva r = Reserva.criar(ReservaId.gerar(), TipoItemReserva.LOCAL, UUID.randomUUID(), intervalo, "desc",
                UUID.randomUUID(), UUID.randomUUID(), null, null, status, Instant.now());
        return r;
    }

    @Test
    @DisplayName("Deve agregar métricas de pendentes, devoluções, ativas hoje e total do mês")
    void deveAgregarMetricas() {
        UUID pastoralId = UUID.randomUUID();
        when(reservaRepository.countPendentesAprovacao(pastoralId)).thenReturn(5L);
        when(reservaRepository.countDevolucoesPendentesHoje(any(), any())).thenReturn(2L);

        Reserva ativa = reserva(StatusReserva.CONFIRMADA);
        Reserva canceladaHoje = reserva(StatusReserva.PENDENTE);
        canceladaHoje.cancelar(UUID.randomUUID(), "motivo detalhado suficiente", false, Instant.now());

        when(reservaRepository.findAll(any(ReservaFiltro.class)))
                .thenReturn(List.of(ativa, canceladaHoje))
                .thenReturn(List.of(ativa, canceladaHoje, reserva(StatusReserva.FINALIZADA)));

        MetricasReservasDto dto = useCase().executar(pastoralId);

        assertThat(dto.pendentesAprovacao()).isEqualTo(5L);
        assertThat(dto.devolucoesPendentesHoje()).isEqualTo(2L);
        assertThat(dto.ativasHoje()).isEqualTo(1L);
        assertThat(dto.totalMesAtual()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Deve funcionar sem filtro de pastoral (pastoralId nulo)")
    void deveFuncionarSemPastoralId() {
        when(reservaRepository.countPendentesAprovacao(null)).thenReturn(0L);
        when(reservaRepository.countDevolucoesPendentesHoje(any(), any())).thenReturn(0L);
        when(reservaRepository.findAll(any(ReservaFiltro.class))).thenReturn(List.of());

        MetricasReservasDto dto = useCase().executar(null);

        assertThat(dto.pendentesAprovacao()).isZero();
        assertThat(dto.totalMesAtual()).isZero();
    }
}
