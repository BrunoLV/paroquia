package br.com.nsfatima.gestao.calendario.api.v1.dto;

import br.com.nsfatima.gestao.calendario.domain.model.TipoSolenidade;

import java.util.UUID;

/**
 * Representação completa de uma solenidade litúrgica cadastrada, usada tanto em telas de
 * administração quanto no provisionamento anual de eventos do calendário eclesiástico.
 *
 * @param tipoSolenidade define se a data é {@code FIXA} (usa {@code mesFixo}/{@code diaFixo}) ou
 *     {@code MOVEL}, calculada a partir do Domingo de Páscoa (usa {@code offsetDiasPascoa})
 * @param offsetDiasPascoa deslocamento em dias em relação ao Domingo de Páscoa (pode ser negativo,
 *     ex: Quarta-feira de Cinzas), preenchido apenas quando {@code tipoSolenidade} é {@code MOVEL}
 * @param fatorDemanda peso relativo de demanda de agenda esperado para o dia da solenidade, usado
 *     nas estimativas do provisionamento anual de eventos
 * @param nivelPrioridade prioridade da solenidade frente a conflitos de agenda no provisionamento
 *     (quanto maior, mais preferência sobre outras atividades no mesmo período)
 * @param ativo indica se a solenidade é considerada pelo provisionamento anual; solenidades
 *     inativas permanecem cadastradas apenas para histórico
 */
public record SolenidadeLiturgicaResponseDTO(
        UUID id,
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
        boolean ativo
) {}
