package br.com.nsfatima.gestao.calendario.domain.type;

import java.util.Locale;

/**
 * Papel que qualifica quem pode decidir formalmente uma solicitação de aprovação de evento
 * (ver {@code ValidarAprovacaoUseCase}/{@code AprovacaoObrigatoriaException}).
 * Em conformidade com o Direito Canônico (Cân. 515 §1), o Pároco detém a alçada decisória ordinária,
 * e o Bispo Diocesano, como autoridade superior, a mesma alçada. Conselhos e coordenações propõem e
 * solicitam, mas não decidem.
 */
public enum AprovadorPapel {
    /** Pároco — aprovador formal com alçada canônica ordinária para decisão final. */
    PAROCO("paroco", "Pároco"),

    /** Bispo Diocesano — autoridade superior, com a mesma alçada decisória do Pároco sobre a paróquia. */
    BISPO_DIOCESANO("bispo-diocesano", "Bispo Diocesano");

    private final String storedCode;
    private final String descricao;

    AprovadorPapel(String storedCode, String descricao) {
        this.storedCode = storedCode;
        this.descricao = descricao;
    }

    /**
     * Resolve o código persistido (ou o nome da constante) para o papel de aprovador
     * correspondente.
     *
     * @param rawValue código bruto persistido ou informado
     * @return papel de aprovador correspondente, ou {@code null} se não reconhecido ou vazio
     */
    public static AprovadorPapel fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT).replace("_", "-");

        for (AprovadorPapel value : values()) {
            if (value.storedCode.equals(normalized) || value.name().equalsIgnoreCase(rawValue.trim())) {
                return value;
            }
        }
        return null;
    }

    /**
     * Determina o papel de aprovador com autoridade canônica decisória.
     * Na governança canônica, o Pároco é a única autoridade decisória final.
     *
     * @param role código do papel do usuário
     * @param organizationType tipo de organização
     * @return AprovadorPapel.PAROCO como alçada exclusiva de homologação
     */
    public static AprovadorPapel resolveForAprovacao(String role, String organizationType) {
        return PAROCO;
    }

    /**
     * Retorna o código persistido correspondente ao papel de aprovador.
     *
     * @return código persistido do papel
     */
    public String storedValue() {
        return storedCode;
    }

    /**
     * Retorna a descrição legível do papel de aprovador.
     *
     * @return descrição legível do papel
     */
    public String getDescricao() {
        return descricao;
    }
}
