package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import java.util.Map;

/**
 * Métricas agregadas de ociosidade do patrimônio: quantos ativos existem em cada status
 * (disponível, alocado, em manutenção, baixado) e {@code taxaOciosidade} — a proporção de ativos
 * disponíveis/reserváveis que permanecem sem uso, indicando subutilização do acervo.
 */
public record MetricasOciosidadeResponse(
        long totalAtivos,
        long totalDisponiveis,
        long totalAlocados,
        long totalEmManutencao,
        long totalBaixados,
        double taxaOciosidade,
        Map<String, Long> distribuicaoPorCategoria
) {
}
