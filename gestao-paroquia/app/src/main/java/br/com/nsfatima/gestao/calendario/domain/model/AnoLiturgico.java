package br.com.nsfatima.gestao.calendario.domain.model;

import java.time.Instant;

/**
 * Representa o registro persistido de um ano litúrgico já provisionado — o ciclo
 * litúrgico e as datas de início/fim são regras eclesiais imutáveis após a
 * criação (ver {@code ImmutableEclesialRuleException}), servindo de referência
 * para validar novos eventos contra o calendário fechado do ano.
 *
 * @param ano ano civil de referência do ano litúrgico
 * @param cicloLetra letra do ciclo litúrgico (A, B ou C) vigente no ano
 * @param dataInicioAdvento instante de início do ano litúrgico (Primeiro Domingo do Advento)
 * @param dataFimComum instante de término do Tempo Comum do ano litúrgico
 * @param atualizadoEmUtc instante da última atualização do registro, em UTC
 */
public record AnoLiturgico(
        Integer ano,
        String cicloLetra,
        Instant dataInicioAdvento,
        Instant dataFimComum,
        Instant atualizadoEmUtc
) {
}
