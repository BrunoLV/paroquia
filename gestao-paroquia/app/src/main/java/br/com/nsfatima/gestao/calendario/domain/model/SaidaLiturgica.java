package br.com.nsfatima.gestao.calendario.domain.model;

import java.time.LocalDate;
import java.util.Map;

/**
 * Resultado do cálculo do ano litúrgico produzido por {@code MotorLiturgico} para
 * um determinado ano nominal — delimita o período do ano litúrgico e traz todos os
 * marcos (solenidades fixas e móveis) já calculados, prontos para serem usados no
 * provisionamento de eventos.
 *
 * @param anoNominal ano civil de referência usado para o cálculo
 * @param dataInicio data de início do ano litúrgico (Primeiro Domingo do Advento)
 * @param dataFim data de término do ano litúrgico
 * @param cicloLetra letra do ciclo litúrgico (A, B ou C) vigente no ano
 * @param marcosCalculados marcos litúrgicos calculados, indexados pela chave técnica do marco
 */
public record SaidaLiturgica(
        int anoNominal,
        LocalDate dataInicio,
        LocalDate dataFim,
        String cicloLetra,
        Map<String, MarcoLiturgico> marcosCalculados
) {
}
