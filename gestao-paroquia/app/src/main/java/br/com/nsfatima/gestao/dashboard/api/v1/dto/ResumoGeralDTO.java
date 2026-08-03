package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Indicadores numéricos de topo do dashboard: volume de eventos do mês corrente, projetos em
 * execução e fila de aprovações pendentes (eventos + projetos), já filtrados pelo escopo
 * organizacional do ator quando aplicável.
 */
@Schema(description = "Resumo geral numérico do dashboard")
public record ResumoGeralDTO(
        @Schema(description = "Total de eventos no mês corrente")
        int totalEventosMes,

        @Schema(description = "Total de projetos ativos (ABERTO ou EM_ANDAMENTO)")
        int projetosAtivos,

        @Schema(description = "Total de solicitações pendentes de aprovação")
        int solicitacoesPendentes
) {
}
