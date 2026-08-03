package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;

/**
 * Status de um evento de calendário conforme exposto em respostas da API. Espelha
 * {@link EventoStatusInput} e acrescenta {@code UNKNOWN_LEGACY} para representar,
 * sem falhar a leitura, um valor persistido no banco que não corresponde a nenhuma
 * constante conhecida (dado legado/inconsistente).
 */
public enum EventoStatusResponse {
    /** Evento ainda em elaboração, não confirmado na agenda. */
    RASCUNHO,
    /** Evento confirmado e válido na agenda. */
    CONFIRMADO,
    /** Evento criado fora do calendário anual regular (ano litúrgico já encerrado). */
    ADICIONADO_EXTRA,
    /** Evento cancelado, não ocupa mais a agenda. */
    CANCELADO,
    /** Valor persistido não reconhecido entre as constantes válidas — indica dado legado/inconsistente. */
    UNKNOWN_LEGACY;

    /**
     * Converte o status de entrada da API para o equivalente de resposta.
     *
     * @param status status de entrada, ou {@code null} para tratar como rascunho
     * @return status de resposta correspondente
     */
    public static EventoStatusResponse fromInput(EventoStatusInput status) {
        return status == null ? RASCUNHO : EventoStatusResponse.valueOf(status.name());
    }

    /**
     * Resolve o valor bruto persistido no banco para a constante correspondente;
     * quando o valor não corresponde a nenhuma constante conhecida, publica um
     * alerta de inconsistência legada e retorna {@code UNKNOWN_LEGACY} em vez de
     * falhar a leitura.
     *
     * @param rawValue valor bruto persistido no banco
     * @param legacyEnumInconsistencyPublisher publicador de alertas de inconsistência de enum legado
     * @param aggregateId identificador do agregado (evento) ao qual o valor pertence
     * @return status de resposta correspondente, ou {@code UNKNOWN_LEGACY} se não reconhecido
     */
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
