package br.com.nsfatima.gestao.calendario.api.v1.dto.metricas;

import java.math.BigDecimal;

/**
 * DTO representativo da Taxa de Conclusão e Abandono de Projetos (CPP/CAE).
 */
public record TaxaConclusaoProjetosResponse(
        Long totalProjetos,
        Long projetosConcluidos,
        Long projetosEmAndamento,
        Long projetosCancelados,
        BigDecimal taxaConclusaoPercentual,
        BigDecimal taxaAbandonoPercentual,
        String avaliacaoExecutiva
) {
}
