package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Ocupação estimada de um ambiente específico na semana. É uma estimativa: considera apenas
 * eventos com local vinculado e uma janela operacional fixa.
 *
 * @param localNome nome do ambiente
 * @param taxaSemanaPercentual percentual (0-100) de ocupação na semana
 * @param horasOcupadasSemana horas somadas de eventos no ambiente na semana
 */
@Schema(description = "Ocupação estimada de um ambiente na semana")
public record OcupacaoLocalDTO(
        String localNome,
        BigDecimal taxaSemanaPercentual,
        long horasOcupadasSemana
) {
}
