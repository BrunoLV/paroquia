package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;

/**
 * Frequência de recorrência de um evento conforme exposta em respostas da API.
 * Espelha {@link FrequenciaRecorrenciaInput} e acrescenta {@code UNKNOWN_LEGACY}
 * para representar, sem falhar a leitura, um valor persistido no banco que não
 * corresponde a nenhuma constante conhecida (dado legado/inconsistente).
 */
public enum FrequenciaRecorrenciaResponse {
    /** Evento se repete todos os dias dentro do período de recorrência. */
    DIARIA,
    /** Evento se repete semanalmente dentro do período de recorrência. */
    SEMANAL,
    /** Evento se repete mensalmente dentro do período de recorrência. */
    MENSAL,
    /** Valor persistido não reconhecido entre as constantes válidas — indica dado legado/inconsistente. */
    UNKNOWN_LEGACY;

    /**
     * Converte a frequência de entrada da API para o equivalente de resposta.
     *
     * @param input frequência de entrada, ou {@code null} para tratar como semanal
     * @return frequência de resposta correspondente
     */
    public static FrequenciaRecorrenciaResponse fromInput(FrequenciaRecorrenciaInput input) {
        return input == null ? SEMANAL : FrequenciaRecorrenciaResponse.valueOf(input.name());
    }

    /**
     * Resolve o valor bruto persistido no banco para a constante correspondente;
     * quando o valor não corresponde a nenhuma constante conhecida, publica um
     * alerta de inconsistência legada e retorna {@code UNKNOWN_LEGACY} em vez de
     * falhar a leitura.
     *
     * @param rawValue valor bruto persistido no banco
     * @param publisher publicador de alertas de inconsistência de enum legado
     * @param aggregateId identificador do agregado (recorrência) ao qual o valor pertence
     * @return frequência de resposta correspondente, ou {@code UNKNOWN_LEGACY} se não reconhecido
     */
    public static FrequenciaRecorrenciaResponse fromStoredValue(
            String rawValue,
            LegacyEnumInconsistencyPublisher publisher,
            String aggregateId) {
        if (rawValue != null) {
            for (FrequenciaRecorrenciaResponse value : values()) {
                if (value == UNKNOWN_LEGACY) {
                    continue;
                }
                if (value.name().equalsIgnoreCase(rawValue.trim())) {
                    return value;
                }
            }
        }

        publisher.publish("recorrencia", aggregateId, "frequencia", rawValue);
        return UNKNOWN_LEGACY;
    }
}
