package br.com.nsfatima.gestao.calendario.api.v1.dto.metricas;

import java.math.BigDecimal;

/**
 * DTO para o indicador de SLA de Despacho Pastoral (Tempo Médio de Aprovação).
 */
public record SlaDespachoPastoralResponse(
        Long totalSolicitacoes,
        Long solicitacoesAprovadas,
        Long solicitacoesRejeitadas,
        BigDecimal tempoMedioAprovacaoHoras,
        BigDecimal slaMetaHoras,
        BigDecimal percentualDentroSla,
        String statusSla
) {
}
