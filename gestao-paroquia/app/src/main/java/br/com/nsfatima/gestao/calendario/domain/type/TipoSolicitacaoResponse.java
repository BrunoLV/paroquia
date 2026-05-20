package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;

public enum TipoSolicitacaoResponse {
    CRIACAO_EVENTO,
    EDICAO_EVENTO,
    ALTERACAO_HORARIO,
    CANCELAMENTO,
    RECLASSIFICACAO,
    OUTRO,
    UNKNOWN_LEGACY;

    public static TipoSolicitacaoResponse fromInput(TipoSolicitacaoInput input) {
        return input == null ? ALTERACAO_HORARIO : TipoSolicitacaoResponse.valueOf(input.name());
    }

    public static TipoSolicitacaoResponse fromStoredValue(
            String rawValue,
            LegacyEnumInconsistencyPublisher publisher,
            String aggregateId) {
        if (rawValue != null) {
            for (TipoSolicitacaoResponse value : values()) {
                if (value == UNKNOWN_LEGACY) {
                    continue;
                }
                if (value.name().equalsIgnoreCase(rawValue.trim())) {
                    return value;
                }
            }
        }

        publisher.publish("aprovacao", aggregateId, "tipoSolicitacao", rawValue);
        return UNKNOWN_LEGACY;
    }
}
