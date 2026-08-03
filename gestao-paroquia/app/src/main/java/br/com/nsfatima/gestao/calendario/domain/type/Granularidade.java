package br.com.nsfatima.gestao.calendario.domain.type;

/**
 * Granularidade temporal usada para agregação de métricas/indicadores do calendário
 * (ex.: taxa de retrabalho, SLA de despacho pastoral), definindo o intervalo pelo
 * qual os dados devem ser agrupados na consulta.
 */
public enum Granularidade {
    /** Agregação por dia. */
    DIARIO,
    /** Agregação por semana. */
    SEMANAL,
    /** Agregação por mês. */
    MENSAL,
    /** Agregação por ano. */
    ANUAL;

    /**
     * Resolve o código textual informado para a constante de granularidade
     * correspondente, comparando de forma case-insensitive.
     *
     * @param rawValue código textual da granularidade
     * @return constante de granularidade correspondente, ou {@code null} se não reconhecida
     */
    public static Granularidade fromCode(String rawValue) {
        for (Granularidade constante : values()) {
            if (constante.name().equalsIgnoreCase(rawValue)) {
                return constante;
            }
        }
        return null;
    }
}
