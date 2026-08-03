package br.com.nsfatima.gestao.calendario.domain.model;

import java.time.LocalDate;

/**
 * Representa um marco (data significativa) do ano litúrgico calculado por
 * {@code MotorLiturgico} — pode ser uma solenidade móvel derivada da Páscoa (ex.:
 * Corpus Christi) ou fixa (ex.: Natal). O {@code fatorDemanda} indica o peso
 * relativo de demanda esperada de agenda/espaços para o dia, usado no
 * provisionamento litúrgico.
 *
 * @param chave identificador técnico único do marco (ex.: {@code PASCOA})
 * @param nome nome litúrgico do marco para exibição
 * @param data data calculada do marco no ano nominal correspondente
 * @param corLiturgica cor litúrgica associada ao marco
 * @param fatorDemanda peso relativo de demanda de agenda esperado para o dia
 */
public record MarcoLiturgico(
        String chave,
        String nome,
        LocalDate data,
        CorLiturgica corLiturgica,
        Double fatorDemanda
) {
}
