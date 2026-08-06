package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Contagem de eventos do mês corrente agrupados por organização responsável, usada para montar
 * o gráfico de distribuição de eventos no dashboard.
 */
@Schema(description = "Distribuição de eventos por organização no mês")
public record DistribuicaoEventosDTO(
        @Schema(description = "Nome da organização responsável")
        String grupoParoquialNome,

        @Schema(description = "Quantidade total de eventos no mês")
        long quantidadeEventos
) {
}
