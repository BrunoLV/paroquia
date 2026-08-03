package br.com.nsfatima.gestao.projeto.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resumo do status de execução dos eventos vinculados a um projeto (realizados vs. pendentes),
 * usado para compor o {@code ProjetoResumoDTO}.
 */
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
