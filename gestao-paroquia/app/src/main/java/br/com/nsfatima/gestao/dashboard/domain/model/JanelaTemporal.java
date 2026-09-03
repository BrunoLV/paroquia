package br.com.nsfatima.gestao.dashboard.domain.model;

import java.time.Instant;

/**
 * Intervalo semiaberto {@code [inicio, fim)} em UTC sobre o qual as métricas de volume do
 * dashboard são calculadas. O limite superior é exclusivo para permitir a concatenação de
 * janelas adjacentes sem dupla contagem de eventos na fronteira.
 *
 * @param inicio instante inicial (inclusive)
 * @param fim instante final (exclusive)
 */
public record JanelaTemporal(Instant inicio, Instant fim) {

    /**
     * Testa a pertinência de um instante ao intervalo, com limite superior exclusivo.
     *
     * @param instante instante a testar
     * @return {@code true} se {@code instante} pertence ao intervalo semiaberto
     */
    public boolean contem(Instant instante) {
        return !instante.isBefore(inicio) && instante.isBefore(fim);
    }
}
