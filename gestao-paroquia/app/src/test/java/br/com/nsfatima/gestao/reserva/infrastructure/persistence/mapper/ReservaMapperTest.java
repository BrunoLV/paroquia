package br.com.nsfatima.gestao.reserva.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.infrastructure.persistence.entity.ReservaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReservaMapperTest {

    private ReservaEntity entityComBuffer(int buffer) {
        Instant inicio = Instant.parse("2026-01-10T10:00:00Z");
        return new ReservaEntity(UUID.randomUUID(), "LOCAL", UUID.randomUUID(), inicio, inicio.plusSeconds(3600),
                buffer, "legada", UUID.randomUUID(), UUID.randomUUID(), null, null, "CONFIRMADA", null, null, null,
                null, null, inicio, inicio, null, 0L);
    }

    @Test
    @DisplayName("Reserva legada com buffer acima do teto carrega com o buffer limitado, sem lançar")
    void limitaBufferLegadoNaLeitura() {
        Reserva r = new ReservaMapper().toDomain(entityComBuffer(600));

        assertThat(r.getIntervalo().bufferMinutos()).isEqualTo(IntervaloReserva.BUFFER_MAXIMO_MINUTOS);
    }

    @Test
    @DisplayName("Buffer dentro do teto é preservado")
    void preservaBufferValido() {
        assertThat(new ReservaMapper().toDomain(entityComBuffer(45)).getIntervalo().bufferMinutos()).isEqualTo(45);
    }
}
