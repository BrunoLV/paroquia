package br.com.nsfatima.gestao.patrimonio.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Value Object do intervalo de uma manutenção: início da indisponibilidade e previsão de retorno.
 * A invariante {@code fimPrevista > inicio} é garantida na construção, refletindo a regra de
 * negócio {@code data_fim_prevista > data_inicio} da especificação.
 */
public record PeriodoManutencao(Instant inicio, Instant fimPrevista) implements Serializable {

    public PeriodoManutencao {
        Objects.requireNonNull(inicio, "A data de início da manutenção é obrigatória.");
        Objects.requireNonNull(fimPrevista, "A data de término previsto da manutenção é obrigatória.");
        if (!fimPrevista.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "A previsão de término da manutenção deve ser posterior ao início.");
        }
    }

    /**
     * Indica se este período se sobrepõe a um intervalo arbitrário (sem margem/buffer),
     * usado para detectar conflito entre manutenção e reservas.
     */
    public boolean colideCom(Instant outroInicio, Instant outroFim) {
        return inicio.isBefore(outroFim) && fimPrevista.isAfter(outroInicio);
    }
}
