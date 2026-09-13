package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

import java.math.BigDecimal;

/**
 * DTO para o Índice de Ociosidade Patrimonial dos bens imóveis da paróquia.
 */
public record OciosidadePatrimonialResponse(
        Long totalLocais,
        BigDecimal totalHorasUteisSemana,
        BigDecimal horasOcupadasSemana,
        BigDecimal horasOciosasSemana,
        BigDecimal indiceOciosidadePercentual,
        String recomendacaoCae
) {
}
