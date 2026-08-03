package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;

/**
 * Tipo de observação de evento conforme exposto em respostas da API. Espelha
 * {@link TipoObservacaoInput} e acrescenta {@code UNKNOWN_LEGACY} para representar,
 * sem falhar a leitura, um valor persistido no banco que não corresponde a nenhuma
 * constante conhecida (dado legado/inconsistente). Apenas {@code NOTA} pode ser
 * criada manualmente pelo usuário (ver {@code ObservacaoTipoManualInvalidoException});
 * os demais tipos são gerados automaticamente pelo sistema ao longo do fluxo de
 * aprovação e mutação de eventos.
 */
public enum TipoObservacaoResponse {
    /** Nota manual registrada diretamente pelo usuário. */
    NOTA,
    /** Justificativa associada a uma solicitação de aprovação. */
    JUSTIFICATIVA,
    /** Registro automático de aprovação de uma solicitação. */
    APROVACAO,
    /** Registro automático de reprovação de uma solicitação. */
    REPROVACAO,
    /** Registro automático de cancelamento do evento. */
    CANCELAMENTO,
    /** Registro automático de alteração de data/horário do evento. */
    AJUSTE_HORARIO,
    /** Valor persistido não reconhecido entre as constantes válidas — indica dado legado/inconsistente. */
    UNKNOWN_LEGACY;

    /**
     * Converte o tipo de observação de entrada da API para o equivalente de resposta.
     *
     * @param input tipo de entrada, ou {@code null} para tratar como nota manual
     * @return tipo de resposta correspondente
     */
    public static TipoObservacaoResponse fromInput(TipoObservacaoInput input) {
        return input == null ? NOTA : TipoObservacaoResponse.valueOf(input.name());
    }

    /**
     * Resolve o valor bruto persistido no banco para a constante correspondente;
     * quando o valor não corresponde a nenhuma constante conhecida, publica um
     * alerta de inconsistência legada e retorna {@code UNKNOWN_LEGACY} em vez de
     * falhar a leitura.
     *
     * @param rawValue valor bruto persistido no banco
     * @param publisher publicador de alertas de inconsistência de enum legado
     * @param aggregateId identificador do agregado (observação) ao qual o valor pertence
     * @return tipo de resposta correspondente, ou {@code UNKNOWN_LEGACY} se não reconhecido
     */
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
