package br.com.nsfatima.gestao.projeto.domain.service;


import java.time.Duration;
import java.time.Instant;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Serviço de domínio para cálculos relacionados à agregação de projetos.
 */
public class ProjetoAgregacaoDomainService {

    private static final BigDecimal RISCO_PERCENTUAL_THRESHOLD = new BigDecimal("80.00");

    /** Calcula o avanço contra uma referência explícita para manter a regra reproduzível. */
    public BigDecimal calcularPercentualTempoDecorrido(Instant inicio, Instant fim, Instant referencia) {
        if (inicio == null || fim == null || referencia == null || !fim.isAfter(inicio)) {
            return BigDecimal.ZERO;
        }
        if (referencia.isBefore(inicio)) {
            return BigDecimal.ZERO;
        }
        if (referencia.isAfter(fim)) {
            return BigDecimal.valueOf(100);
        }
        return BigDecimal.valueOf(Duration.between(inicio, referencia).toMillis())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(Duration.between(inicio, fim).toMillis()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o percentual de tempo decorrido do projeto até agora. Delega para a sobrecarga com
     * referência explícita usando {@link Instant#now()}.
     *
     * @param inicio Data de início do projeto
     * @param fim    Data de término do projeto
     * @return Percentual (0-100) de tempo decorrido
     */
    public BigDecimal calcularPercentualTempoDecorrido(Instant inicio, Instant fim) {
        return calcularPercentualTempoDecorrido(inicio, fim, Instant.now());
    }

    /**
     * Verifica se o projeto está em risco baseado no tempo decorrido e pendências.
     *
     * @param inicio           Data de início
     * @param fim              Data de término
     * @param eventosPendentes Quantidade de eventos ainda pendentes
     * @return true se estiver em risco, false caso contrário
     */
    public boolean verificarSeEstaEmRisco(Instant inicio, Instant fim, int eventosPendentes) {
        if (eventosPendentes <= 0) {
            return false;
        }

        BigDecimal percentual = calcularPercentualTempoDecorrido(inicio, fim);
        return percentual.compareTo(RISCO_PERCENTUAL_THRESHOLD) > 0;
    }
}
