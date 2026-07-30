package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

/**
 * DTO para o Índice de Ociosidade Patrimonial dos bens imóveis da paróquia.
 */
public record OciosidadePatrimonialResponse(
        Long totalLocais,
        Double totalHorasUteisSemana,
        Double horasOcupadasSemana,
        Double horasOciosasSemana,
        Double indiceOciosidadePercentual,
        String recomendacaoCae
) {
}
