package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO contendo todas as métricas consolidadas do dashboard")
public record DashboardMetricsDTO(
        @Schema(description = "Resumo geral numérico")
        ResumoGeralDTO resumoGeral,

        @Schema(description = "Distribuição de eventos por organização no mês")
        List<DistribuicaoEventosDTO> eventosPorOrganizacao,

        @Schema(description = "Detalhes de execução e progresso dos projetos ativos")
        List<DetalheProjetoDTO> projetosAtivosDetalhes,

        @Schema(description = "Eventos agendados para a semana corrente")
        List<EventoSemanalDTO> eventosSemana
) {
}
