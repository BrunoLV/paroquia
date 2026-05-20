package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;

public enum EventoStatusResponse {
    RASCUNHO,
    CONFIRMADO,
    ADICIONADO_EXTRA,
    CANCELADO,
    UNKNOWN_LEGACY;

    public static EventoStatusResponse fromInput(EventoStatusInput status) {
        return status == null ? RASCUNHO : EventoStatusResponse.valueOf(status.name());
    }

    public static EventoStatusResponse fromStoredValue(
            String rawValue,
            LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher,
            String aggregateId) {
        if (rawValue == null || rawValue.isBlank()) {
            legacyEnumInconsistencyPublisher.publish("evento", aggregateId, "status", rawValue);
            return UNKNOWN_LEGACY;
        }

        for (EventoStatusResponse status : values()) {
            if (status == UNKNOWN_LEGACY) {
                continue;
            }
            if (status.name().equalsIgnoreCase(rawValue.trim())) {
                return status;
            }
        }

        legacyEnumInconsistencyPublisher.publish("evento", aggregateId, "status", rawValue);
        return UNKNOWN_LEGACY;
    }
}
