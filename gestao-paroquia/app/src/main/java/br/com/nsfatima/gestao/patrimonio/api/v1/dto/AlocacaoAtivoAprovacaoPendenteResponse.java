package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import java.util.UUID;

/**
 * Retornado quando uma solicitação de alocação de ativo não pode ser efetivada de imediato e é
 * desviada para o fluxo de aprovação (ator sem alçada suficiente) — {@code solicitacaoAprovacaoId}
 * referencia a {@code AprovacaoEntity} criada, a ser decidida posteriormente.
 */
public record AlocacaoAtivoAprovacaoPendenteResponse(
        UUID solicitacaoAprovacaoId,
        String status,
        UUID ativoId,
        UUID eventoId,
        String mensagem) {
}
