package br.com.nsfatima.gestao.calendario.domain.type;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Tipo de solicitação de aprovação, conforme recebido em requisições da API — cada
 * valor representa uma categoria de mutação que exige decisão de aprovação
 * (ver {@code AprovacaoObrigatoriaException}) antes de ser aplicada. Corresponde ao
 * par de saída {@link TipoSolicitacaoResponse}.
 */
public enum TipoSolicitacaoInput {
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
    /** Solicitação de alocação de ativo patrimonial por empréstimo a um evento. */
    ALOCACAO_ATIVO_EMPRESTIMO,
    /** Demais tipos de solicitação não cobertos pelas categorias específicas. */
    OUTRO;

    /**
     * Normaliza o valor bruto recebido em JSON para a constante correspondente,
     * tolerando variações de caixa/formatação.
     *
     * @param rawValue valor textual recebido na requisição
     * @return constante de tipo de solicitação normalizada
     */
    @JsonCreator
    public static TipoSolicitacaoInput fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, TipoSolicitacaoInput.class);
    }
}
