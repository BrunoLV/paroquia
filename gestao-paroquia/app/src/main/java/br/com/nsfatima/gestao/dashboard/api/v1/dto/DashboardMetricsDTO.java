package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Payload consolidado retornado pelo endpoint de dashboard, agregando as quatro visões
 * exibidas na tela inicial: resumo numérico, distribuição de eventos por organização,
 * progresso dos projetos ativos e agenda da semana corrente.
 */
@Schema(description = "DTO contendo todas as métricas consolidadas do dashboard")
public record DashboardMetricsDTO(
        @Schema(description = "Resumo geral numérico")
        ResumoGeralDTO resumoGeral,

        @Schema(description = "Distribuição de eventos por organização no mês")
        List<DistribuicaoEventosDTO> eventosPorGrupoParoquial,

        @Schema(description = "Detalhes de execução e progresso dos projetos ativos")
        List<DetalheProjetoDTO> projetosAtivosDetalhes,

        @Schema(description = "Eventos agendados para a semana corrente")
        List<EventoSemanalDTO> eventosSemana
) {
}
