package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import java.util.UUID;

/**
 * Resposta retornada quando o cancelamento de um evento não é aplicado diretamente, mas roteado
 * para o fluxo de aprovação por já se tratar de um evento aprovado. Indica ao chamador que o
 * cancelamento ficou pendente de decisão em vez de ter sido efetivado imediatamente.
 *
 * @param solicitacaoAprovacaoId identificador da {@code AprovacaoEntity} criada para decidir o
 *     cancelamento
 * @param status status atual da solicitação de aprovação (ex: "PENDENTE")
 * @param eventoId identificador do evento cujo cancelamento está pendente de aprovação
 * @param mensagem mensagem descritiva para exibição ao usuário sobre o encaminhamento do pedido
 */
public record CancelamentoPendenteResponse(
        UUID solicitacaoAprovacaoId,
        String status,
        UUID eventoId,
        String mensagem) {
}
