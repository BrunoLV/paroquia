package br.com.nsfatima.gestao.patrimonio.domain.event;

import java.util.UUID;

/**
 * Publicado quando um pedido de alocação de ativo patrimonial vira um empréstimo entre
 * organizações e fica pendente de aprovação, para disparar notificações ao solicitante/aprovador.
 */
public record AlocacaoAtivoPendenteEvent(
        UUID aprovacaoId,
        UUID ativoId,
        UUID eventoId,
        String solicitanteId) {
}
