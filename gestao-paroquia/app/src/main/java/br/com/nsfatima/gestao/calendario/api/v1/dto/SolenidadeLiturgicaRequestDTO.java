package br.com.nsfatima.gestao.calendario.api.v1.dto;

import br.com.nsfatima.gestao.calendario.domain.model.TipoSolenidade;

/**
 * Dados para criação ou atualização de uma solenidade litúrgica, usada como base para o
 * provisionamento anual de eventos do calendário eclesiástico.
 *
 * @param tipoSolenidade define se a data é {@code FIXA} (usa {@code mesFixo}/{@code diaFixo}) ou
 *     {@code MOVEL}, calculada a partir do Domingo de Páscoa (usa {@code offsetDiasPascoa})
 * @param mesFixo mês fixo (1-12) da solenidade, aplicável apenas quando {@code tipoSolenidade} é
 *     {@code FIXA}
 * @param diaFixo dia fixo do mês da solenidade, aplicável apenas quando {@code tipoSolenidade} é
 *     {@code FIXA}
 * @param offsetDiasPascoa deslocamento em dias em relação ao Domingo de Páscoa (pode ser negativo,
 *     ex: Quarta-feira de Cinzas), aplicável apenas quando {@code tipoSolenidade} é {@code MOVEL}
 * @param fatorDemanda multiplicador usado para estimar a demanda por espaços/recursos em torno desta
 *     solenidade durante o provisionamento anual de eventos
 * @param nivelPrioridade prioridade da solenidade frente a conflitos de agenda no provisionamento
 *     (quanto maior, mais preferência sobre outras atividades no mesmo período)
 */
public record SolenidadeLiturgicaRequestDTO(
        String chave,
        String nome,
        TipoSolenidade tipoSolenidade,
        Integer mesFixo,
        Integer diaFixo,
        Integer offsetDiasPascoa,
        String corLiturgica,
        Double fatorDemanda,
        Integer nivelPrioridade,
        String descricao,
        Boolean ativo
) {}
