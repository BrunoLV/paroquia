package br.com.nsfatima.gestao.reserva.domain.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Value Object representativo da janela temporal de uma reserva e sua respectiva margem logística (buffer).
 * Invariante: término deve ser estritamente posterior ao início e buffer não negativo.
 */
public record IntervaloReserva(
        Instant dataHoraInicio,
        Instant dataHoraFim,
        int bufferMinutos
) implements Serializable {

    public static final int BUFFER_PADRAO_MINUTOS = 30;

    public IntervaloReserva {
        Objects.requireNonNull(dataHoraInicio, "Data/hora de início da reserva é obrigatória.");
        Objects.requireNonNull(dataHoraFim, "Data/hora de término da reserva é obrigatória.");
        if (!dataHoraFim.isAfter(dataHoraInicio)) {
            throw new IllegalArgumentException("A data/hora de término deve ser estritamente posterior ao início da reserva.");
        }
        if (bufferMinutos < 0) {
            throw new IllegalArgumentException("O buffer de minutos não pode ser negativo.");
        }
    }

    public static IntervaloReserva de(Instant inicio, Instant fim) {
        return new IntervaloReserva(inicio, fim, BUFFER_PADRAO_MINUTOS);
    }

    /**
     * Calcula o limite superior do intervalo ocupado real, incluindo o tempo de carência para organização/limpeza.
     */
    public Instant fimComBuffer() {
        return dataHoraFim.plus(Duration.ofMinutes(bufferMinutos));
    }

    /**
     * Regra matemática formal de conflito de agenda considerando buffers:
     * NovoInicio < (ExistenteFim + BufferExistente) AND (NovoFim + BufferNovo) > ExistenteInicio
     */
    public boolean colideCom(IntervaloReserva outro) {
        Objects.requireNonNull(outro, "O intervalo para comparação de colisão não pode ser nulo.");
        return this.dataHoraInicio.isBefore(outro.fimComBuffer()) && this.fimComBuffer().isAfter(outro.dataHoraInicio());
    }
}
