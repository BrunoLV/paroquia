package br.com.nsfatima.gestao.reserva.domain.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Value Object representativo da janela temporal de uma reserva e sua respectiva margem logística (buffer).
 * Invariante: término estritamente posterior ao início e buffer entre 0 e {@link #BUFFER_MAXIMO_MINUTOS}.
 */
public record IntervaloReserva(
        Instant dataHoraInicio,
        Instant dataHoraFim,
        int bufferMinutos
) implements Serializable {

    public static final int BUFFER_PADRAO_MINUTOS = 30;

    /**
     * Teto da margem logística. Limita o alcance de uma reserva existente sobre a agenda vizinha e é o que
     * permite à consulta de conflitos usar uma janela fixa de busca sem perder colisões.
     */
    public static final int BUFFER_MAXIMO_MINUTOS = 240;

    public IntervaloReserva {
        Objects.requireNonNull(dataHoraInicio, "Data/hora de início da reserva é obrigatória.");
        Objects.requireNonNull(dataHoraFim, "Data/hora de término da reserva é obrigatória.");
        if (!dataHoraFim.isAfter(dataHoraInicio)) {
            throw new IllegalArgumentException(
                    "A data/hora de término deve ser estritamente posterior ao início da reserva.");
        }
        if (bufferMinutos < 0) {
            throw new IllegalArgumentException("O buffer de minutos não pode ser negativo.");
        }
        if (bufferMinutos > BUFFER_MAXIMO_MINUTOS) {
            throw new IllegalArgumentException(
                    "O buffer de minutos não pode exceder " + BUFFER_MAXIMO_MINUTOS + " minutos.");
        }
    }

    public static IntervaloReserva de(Instant inicio, Instant fim) {
        return new IntervaloReserva(inicio, fim, BUFFER_PADRAO_MINUTOS);
    }

    public static IntervaloReserva de(Instant inicio, Instant fim, int bufferMinutos) {
        return new IntervaloReserva(inicio, fim, bufferMinutos);
    }

    /**
     * Calcula o limite superior do intervalo ocupado real, incluindo o tempo de carência para organização/limpeza.
     */
    public Instant fimComBuffer() {
        return dataHoraFim.plus(Duration.ofMinutes(bufferMinutos));
    }

    /**
     * Regra matemática formal de conflito de agenda considerando buffers.
     * NovoInicio &lt; (ExistenteFim + BufferExistente) AND (NovoFim + BufferNovo) &gt; ExistenteInicio
     */
    public boolean colideCom(IntervaloReserva outro) {
        Objects.requireNonNull(outro, "O intervalo para comparação de colisão não pode ser nulo.");
        return this.dataHoraInicio.isBefore(outro.fimComBuffer())
                && this.fimComBuffer().isAfter(outro.dataHoraInicio());
    }
}
