package br.com.nsfatima.gestao.reserva.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservaTest {

    @Test
    @DisplayName("Deve criar reserva com estado PENDENTE e dados corretos")
    void deveCriarReservaComEstadoPendente() {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        Instant fim = inicio.plus(1, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, fim, 15);
        UUID itemId = UUID.randomUUID();
        UUID solicitanteId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        Reserva reserva = Reserva.criar(
                ReservaId.generate(),
                TipoItemReserva.LOCAL,
                itemId,
                intervalo,
                "Ensaio Coral",
                solicitanteId,
                orgId,
                null,
                null,
                StatusReserva.PENDENTE,
                Instant.now()
        );

        assertThat(reserva.getId()).isNotNull();
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(reserva.getTipoItem()).isEqualTo(TipoItemReserva.LOCAL);
        assertThat(reserva.getItemId()).isEqualTo(itemId);
        assertThat(reserva.getIntervalo().bufferMinutos()).isEqualTo(15);
        assertThat(reserva.getIntervalo().fimComBuffer()).isEqualTo(fim.plus(15, ChronoUnit.MINUTES));
    }

    @Test
    @DisplayName("Deve aprovar reserva pendente com sucesso")
    void deveAprovarReservaPendente() {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(2, ChronoUnit.HOURS), 0);
        Reserva reserva = Reserva.criar(
                ReservaId.generate(),
                TipoItemReserva.LOCAL,
                UUID.randomUUID(),
                intervalo,
                "Reunião",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                StatusReserva.PENDENTE,
                Instant.now()
        );

        UUID aprovadorId = UUID.randomUUID();
        Instant agora = Instant.now();
        reserva.aprovar(aprovadorId, agora);

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CONFIRMADA);
        assertThat(reserva.getAprovadorId()).isEqualTo(aprovadorId);
        assertThat(reserva.getAprovadoEm()).isEqualTo(agora);
    }

    @Test
    @DisplayName("Não deve permitir aprovar reserva já cancelada")
    void naoDeveAprovarReservaCancelada() {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(2, ChronoUnit.HOURS), 0);
        Reserva reserva = Reserva.criar(
                ReservaId.generate(),
                TipoItemReserva.LOCAL,
                UUID.randomUUID(),
                intervalo,
                "Reunião",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                StatusReserva.PENDENTE,
                Instant.now()
        );

        reserva.cancelar(UUID.randomUUID(), "Mudança de planos dos organizadores", false, Instant.now());

        assertThatThrownBy(() -> reserva.aprovar(UUID.randomUUID(), Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Apenas reservas com status PENDENTE");
    }

    @Test
    @DisplayName("Deve registrar retirada e devolução para ativo patrimonial")
    void deveRegistrarRetiradaEDevolucaoParaAtivo() {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(2, ChronoUnit.HOURS), 0);
        Reserva reserva = Reserva.criar(
                ReservaId.generate(),
                TipoItemReserva.ATIVO,
                UUID.randomUUID(),
                intervalo,
                "Som",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                StatusReserva.PENDENTE,
                Instant.now()
        );

        reserva.aprovar(UUID.randomUUID(), Instant.now());
        reserva.registrarRetirada(UUID.randomUUID(), Instant.now());
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.RETIRADO);

        reserva.registrarDevolucao(UUID.randomUUID(), "Cabo P10 com chiado leve", Instant.now());
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.FINALIZADA);
        assertThat(reserva.getCheckAvarias()).isEqualTo("Cabo P10 com chiado leve");
    }

    @Test
    @DisplayName("Não deve permitir registrar retirada para local")
    void naoDevePermitirRetiradaParaLocal() {
        Instant inicio = Instant.now().plus(2, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(2, ChronoUnit.HOURS), 0);
        Reserva reserva = Reserva.criar(
                ReservaId.generate(),
                TipoItemReserva.LOCAL,
                UUID.randomUUID(),
                intervalo,
                "Salão",
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                StatusReserva.CONFIRMADA,
                Instant.now()
        );

        assertThatThrownBy(() -> reserva.registrarRetirada(UUID.randomUUID(), Instant.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exclusivamente a bens patrimoniais");
    }
}
