package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Quantidade de solicitações pendentes de homologação de um determinado tipo (agrega aprovações
 * de evento e de projeto sob o mesmo rótulo de tipo).
 *
 * @param tipo tipo da solicitação (ex.: {@code CRIACAO_EVENTO}, {@code ALTERACAO_HORARIO})
 * @param quantidade solicitações pendentes desse tipo
 */
@Schema(description = "Pendências de homologação agrupadas por tipo de solicitação")
public record PendenciaHomologacaoDTO(
        String tipo,
        long quantidade
) {
}
