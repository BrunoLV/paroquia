package br.com.nsfatima.gestao.calendario.domain.model;

import java.util.UUID;

/**
 * Modelo de domínio imutável que representa um Ciclo ou Festa Litúrgica.
 *
 * @param id Identificador único do ciclo
 * @param nome Nome do ciclo ou festa
 * @param tipoCiclo Tipo do ciclo litúrgico
 * @param corLiturgica Cor vestimentar litúrgica associada
 * @param mesInicio Mês de início (1-12)
 * @param diaInicio Dia de início (1-31)
 * @param mesFim Mês de término (1-12)
 * @param diaFim Dia de término (1-31)
 * @param fatorDemanda Fator multiplicador de demanda pastoral/operacional
 * @param descricao Descrição sumária das características do período
 */
public record CicloLiturgico(
        UUID id,
        String nome,
        TipoCicloLiturgico tipoCiclo,
        String corLiturgica,
        int mesInicio,
        int diaInicio,
        int mesFim,
        int diaFim,
        double fatorDemanda,
        String descricao
) {
}
