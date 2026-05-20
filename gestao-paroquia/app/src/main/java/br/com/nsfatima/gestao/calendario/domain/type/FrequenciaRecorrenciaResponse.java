package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;

public enum FrequenciaRecorrenciaResponse {
    DIARIA,
    SEMANAL,
    MENSAL,
    UNKNOWN_LEGACY;

    public static FrequenciaRecorrenciaResponse fromInput(FrequenciaRecorrenciaInput input) {
        return input == null ? SEMANAL : FrequenciaRecorrenciaResponse.valueOf(input.name());
    }

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
