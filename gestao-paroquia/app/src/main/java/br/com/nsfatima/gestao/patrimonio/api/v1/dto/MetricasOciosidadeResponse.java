package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import java.math.BigDecimal;

/**
 * Métricas agregadas de ociosidade do acervo: contagem de ativos por status e
 * {@code taxaOciosidade} — proporção de bens {@code DISPONIVEL} (sem uso) sobre o total incorporado.
 */
public record MetricasOciosidadeResponse(
        long totalAtivos,
        long totalDisponiveis,
        long totalReservados,
        long totalEmManutencao,
        BigDecimal taxaOciosidade
) {
}
