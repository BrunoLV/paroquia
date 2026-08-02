package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Distribuição de eventos por organização no mês")
public record DistribuicaoEventosDTO(
        @Schema(description = "Nome da organização responsável")
        String organizacaoNome,

        @Schema(description = "Quantidade total de eventos no mês")
        long quantidadeEventos
) {
}
