package br.com.nsfatima.gestao.reserva.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservaCicloDeVidaTest {

    private static final Instant BASE = Instant.parse("2026-03-10T12:00:00Z");

    private Reserva reserva(TipoItemReserva tipo, StatusReserva status, Instant criadaEm, Instant inicio) {
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(Duration.ofHours(2)), 0);
        return Reserva.criar(ReservaId.gerar(), tipo, UUID.randomUUID(), intervalo, "desc",
                UUID.randomUUID(), UUID.randomUUID(), null, null, status, criadaEm);
    }

    @Test
    @DisplayName("Intervalo rejeita margem logística acima do máximo que sustenta a janela de consulta de conflitos")
    void intervaloRejeitaBufferAcimaDoMaximo() {
        Instant inicio = BASE.plus(Duration.ofDays(1));
        Instant fim = inicio.plus(Duration.ofHours(1));

        assertThat(new IntervaloReserva(inicio, fim, IntervaloReserva.BUFFER_MAXIMO_MINUTOS).bufferMinutos())
                .isEqualTo(IntervaloReserva.BUFFER_MAXIMO_MINUTOS);
        assertThatThrownBy(() -> new IntervaloReserva(inicio, fim, IntervaloReserva.BUFFER_MAXIMO_MINUTOS + 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Reserva RETIRADO (bem em custódia) não pode ser cancelada")
    void naoCancelaReservaRetirada() {
        Reserva r = reserva(TipoItemReserva.ATIVO, StatusReserva.CONFIRMADA, BASE, BASE.plus(Duration.ofDays(5)));
        r.registrarRetirada(UUID.randomUUID(), BASE);

        assertThatThrownBy(() -> r.cancelar(UUID.randomUUID(), "motivo suficientemente longo", false, BASE))
                .isInstanceOf(IllegalStateException.class);
        assertThat(r.getStatus()).isEqualTo(StatusReserva.RETIRADO);
    }

    @Test
    @DisplayName("Pedido feito com folga vence o prazo de análise 48 horas antes do início")
    void prazoDeAnaliseComFolgaEQuarentaEOitoHorasAntes() {
        Instant inicio = BASE.plus(Duration.ofDays(10));
        Reserva r = reserva(TipoItemReserva.LOCAL, StatusReserva.PENDENTE, BASE, inicio);

        assertThat(r.prazoDeAnalise()).isEqualTo(inicio.minus(Duration.ofHours(48)));
        assertThat(r.analiseVencida(inicio.minus(Duration.ofHours(49)))).isFalse();
        assertThat(r.analiseVencida(inicio.minus(Duration.ofHours(48)))).isTrue();
    }

    @Test
    @DisplayName("Pedido feito com menos de 48 horas de antecedência só vence no início, sem expiração imediata")
    void pedidoTardioSoVenceNoInicio() {
        Instant inicio = BASE.plus(Duration.ofHours(10));
        Reserva r = reserva(TipoItemReserva.LOCAL, StatusReserva.PENDENTE, BASE, inicio);

        assertThat(r.prazoDeAnalise()).isEqualTo(inicio);
        assertThat(r.analiseVencida(BASE.plus(Duration.ofMinutes(15)))).isFalse();
        assertThat(r.analiseVencida(inicio)).isTrue();
    }

    @Test
    @DisplayName("Apenas reserva PENDENTE tem análise vencida")
    void naoPendenteNuncaTemAnaliseVencida() {
        Reserva r = reserva(TipoItemReserva.LOCAL, StatusReserva.CONFIRMADA, BASE, BASE.plus(Duration.ofDays(1)));

        assertThat(r.analiseVencida(BASE.plus(Duration.ofDays(30)))).isFalse();
    }

    @Test
    @DisplayName("Uso de local só é finalizado depois de iniciado")
    void naoFinalizaUsoDeLocalQueAindaNaoComecou() {
        Instant inicio = BASE.plus(Duration.ofDays(1));
        Reserva r = reserva(TipoItemReserva.LOCAL, StatusReserva.CONFIRMADA, BASE, inicio);

        assertThatThrownBy(() -> r.finalizarUsoLocal(inicio.minusSeconds(1)))
                .isInstanceOf(IllegalStateException.class);

        r.finalizarUsoLocal(inicio.plus(Duration.ofHours(3)));
        assertThat(r.getStatus()).isEqualTo(StatusReserva.FINALIZADA);
    }
}
