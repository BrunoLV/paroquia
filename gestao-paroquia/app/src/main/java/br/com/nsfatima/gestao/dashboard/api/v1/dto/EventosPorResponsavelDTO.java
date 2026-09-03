package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Quantidade de eventos liderados por uma organização/pastoral no período do filtro.
 *
 * @param grupoParoquialNome nome da organização responsável
 * @param quantidade eventos liderados no período
 */
@Schema(description = "Contagem de eventos por organização responsável no período")
public record EventosPorResponsavelDTO(
        String grupoParoquialNome,
        long quantidade
) {
}
