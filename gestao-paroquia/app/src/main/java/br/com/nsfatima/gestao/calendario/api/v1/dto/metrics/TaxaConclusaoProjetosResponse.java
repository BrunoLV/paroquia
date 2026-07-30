package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

/**
 * DTO representativo da Taxa de Conclusão e Abandono de Projetos (CPP/CAE).
 */
public record TaxaConclusaoProjetosResponse(
        Long totalProjetos,
        Long projetosConcluidos,
        Long projetosEmAndamento,
        Long projetosCancelados,
        Double taxaConclusaoPercentual,
        Double taxaAbandonoPercentual,
        String avaliacaoExecutiva
) {
}
