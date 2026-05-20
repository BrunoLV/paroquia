package br.com.nsfatima.gestao.projeto.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo do status de execução dos eventos do projeto")
public record StatusExecucaoDTO(
        @Schema(description = "Total de eventos vinculados ao projeto")
        int totalEventos,

        @Schema(description = "Total de eventos que já ocorreram")
        int eventosRealizados,

        @Schema(description = "Total de eventos que ainda não ocorreram")
        int eventosPendentes
) {
}
