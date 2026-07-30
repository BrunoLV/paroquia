package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

/**
 * DTO para o indicador de SLA de Despacho Pastoral (Tempo Médio de Aprovação).
 */
public record SlaDespachoPastoralResponse(
        Long totalSolicitacoes,
        Long solicitacoesAprovadas,
        Long solicitacoesRejeitadas,
        Double tempoMedioAprovacaoHoras,
        Double slaMetaHoras,
        Double percentualDentroSla,
        String statusSla
) {
}
