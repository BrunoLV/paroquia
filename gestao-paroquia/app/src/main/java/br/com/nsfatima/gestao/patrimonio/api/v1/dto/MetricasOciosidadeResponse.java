package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import java.util.Map;

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
