package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Detalhes de um projeto ativo para exibição no dashboard")
public record DetalheProjetoDTO(
        @Schema(description = "Nome do projeto")
        String nome,

        @Schema(description = "Status atual do projeto")
        String status,

        @Schema(description = "Percentual de tempo decorrido do projeto")
        double percentualTempoDecorrido,

        @Schema(description = "Total de eventos vinculados ao projeto")
        int totalEventos,

        @Schema(description = "Total de eventos que já ocorreram")
        int eventosRealizados,

        @Schema(description = "Indica se o projeto está em risco (prazo apertado com pendências)")
        boolean emRisco,

        @Schema(description = "Nomes das pastorais envolvidas no projeto")
        List<String> envolvidos
) {
}
