package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import java.util.UUID;

/**
 * Resposta retornada quando uma solicitação de criação ou alteração de evento é roteada para o
 * fluxo de aprovação em vez de ser aplicada de imediato, sinalizando ao chamador que a operação
 * ficou pendente de decisão por um aprovador.
 *
 * @param solicitacaoAprovacaoId identificador da {@code AprovacaoEntity} criada para decidir a
 *     solicitação
 * @param status status atual da solicitação de aprovação (ex: "PENDENTE")
 * @param mensagem mensagem descritiva para exibição ao usuário sobre o encaminhamento do pedido
 */
public record EventoAprovacaoPendenteResponse(
        UUID solicitacaoAprovacaoId,
        String status,
        String mensagem) {
}
