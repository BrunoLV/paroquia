package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;

public enum TipoObservacaoResponse {
    NOTA,
    JUSTIFICATIVA,
    APROVACAO,
    REPROVACAO,
    CANCELAMENTO,
    AJUSTE_HORARIO,
    UNKNOWN_LEGACY;

    public static TipoObservacaoResponse fromInput(TipoObservacaoInput input) {
        return input == null ? NOTA : TipoObservacaoResponse.valueOf(input.name());
    }

    public static TipoObservacaoResponse fromStoredValue(
            String rawValue,
            LegacyEnumInconsistencyPublisher publisher,
            String aggregateId) {
        if (rawValue != null) {
            for (TipoObservacaoResponse value : values()) {
                if (value == UNKNOWN_LEGACY) {
                    continue;
                }
                if (value.name().equalsIgnoreCase(rawValue.trim())) {
                    return value;
                }
            }
        }

        publisher.publish("observacao", aggregateId, "tipo", rawValue);
        return UNKNOWN_LEGACY;
    }
}
