package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo geral numérico do dashboard")
public record GeneralSummaryDTO(
    @Schema(description = "Total de eventos no mês corrente")
    int totalEventosMes,

    @Schema(description = "Total de projetos ativos (ABERTO ou EM_ANDAMENTO)")
    int projetosAtivos,

    @Schema(description = "Total de solicitações pendentes de aprovação")
    int solicitacoesPendentes
) {}
