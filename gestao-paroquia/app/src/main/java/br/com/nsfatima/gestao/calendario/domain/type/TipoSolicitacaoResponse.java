package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;

/**
 * Tipo de solicitação de aprovação conforme exposto em respostas da API. Espelha
 * {@link TipoSolicitacaoInput} e acrescenta {@code UNKNOWN_LEGACY} para representar,
 * sem falhar a leitura, um valor persistido no banco que não corresponde a nenhuma
 * constante conhecida (dado legado/inconsistente). Não inclui
 * {@code ALOCACAO_ATIVO_EMPRESTIMO} do par de entrada, pois solicitações desse tipo
 * ainda não possuem representação própria no contrato de resposta.
 */
public enum TipoSolicitacaoResponse {
    /** Solicitação de criação de um novo evento. */
    CRIACAO_EVENTO,
    /** Solicitação de edição de dados de um evento existente. */
    EDICAO_EVENTO,
    /** Solicitação de alteração de data/horário de um evento existente. */
    ALTERACAO_HORARIO,
    /** Solicitação de cancelamento de um evento. */
    CANCELAMENTO,
    /** Solicitação de reclassificação de categoria/prioridade de um evento. */
    RECLASSIFICACAO,
    /** Demais tipos de solicitação não cobertos pelas categorias específicas. */
    OUTRO,
    /** Valor persistido não reconhecido entre as constantes válidas — indica dado legado/inconsistente. */
    UNKNOWN_LEGACY;

    /**
     * Converte o tipo de solicitação de entrada da API para o equivalente de resposta.
     *
     * @param input tipo de entrada, ou {@code null} para tratar como alteração de horário
     * @return tipo de resposta correspondente
     */
    public static TipoSolicitacaoResponse fromInput(TipoSolicitacaoInput input) {
        return input == null ? ALTERACAO_HORARIO : TipoSolicitacaoResponse.valueOf(input.name());
    }

    /**
     * Resolve o valor bruto persistido no banco para a constante correspondente;
     * quando o valor não corresponde a nenhuma constante conhecida, publica um
     * alerta de inconsistência legada e retorna {@code UNKNOWN_LEGACY} em vez de
     * falhar a leitura.
     *
     * @param rawValue valor bruto persistido no banco
     * @param publisher publicador de alertas de inconsistência de enum legado
     * @param aggregateId identificador do agregado (aprovação) ao qual o valor pertence
     * @return tipo de resposta correspondente, ou {@code UNKNOWN_LEGACY} se não reconhecido
     */
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
