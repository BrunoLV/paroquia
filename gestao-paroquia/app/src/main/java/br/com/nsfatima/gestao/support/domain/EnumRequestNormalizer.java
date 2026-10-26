package br.com.nsfatima.gestao.support.domain;

/**
 * Normaliza valores textuais recebidos em parâmetros de requisição (query params, path variables)
 * para constantes de enum, tolerando variação de caixa (case-insensitive) e espaços em branco nas
 * extremidades — evitando que clientes da API precisem enviar o valor exatamente como
 * {@link Enum#name()}. Não trata aliases/sinônimos: o valor normalizado deve corresponder a um dos
 * nomes das constantes do enum, apenas ignorando maiúsculas/minúsculas.
 */
public final class EnumRequestNormalizer {

    private EnumRequestNormalizer() {
    }

    /**
     * Converte o valor textual bruto na constante correspondente do enum informado, comparando de
     * forma case-insensitive após remover espaços em branco nas extremidades.
     *
     * @param <E> tipo do enum alvo
     * @param rawValue valor textual recebido na requisição; pode ser {@code null}
     * @param enumType classe do enum alvo da conversão
     * @return a constante do enum correspondente, ou {@code null} se {@code rawValue} for
     *     {@code null}
     * @throws IllegalArgumentException se {@code rawValue}, após remoção de espaços, for vazio, ou
     *     se não corresponder (ignorando caixa) a nenhuma constante de {@code enumType}
     */
    public static <E extends Enum<E>> E normalize(String rawValue, Class<E> enumType) {
        if (rawValue == null) {
            return null;
        }

        String candidate = rawValue.trim();
        if (candidate.isEmpty()) {
            throw new IllegalArgumentException("Valor do enum não pode ser vazio");
        }

        for (E constant : enumType.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(candidate)) {
                return constant;
            }
        }

        throw new IllegalArgumentException(
                "Unsupported enum value '%s' for %s".formatted(rawValue, enumType.getSimpleName()));
    }
}
