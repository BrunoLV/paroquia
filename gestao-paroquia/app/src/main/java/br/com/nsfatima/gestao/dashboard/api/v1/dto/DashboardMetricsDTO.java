package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Payload consolidado retornado pelo endpoint de métricas do dashboard, agregando as visões da
 * seção crítica e da seção de projetos: resumo numérico, distribuição de eventos por organização,
 * distribuição de projetos por status, progresso dos projetos ativos, eventos sem projeto e a
 * agenda da semana corrente. Todas as coleções já vêm filtradas pelo recorte temporal e pelo
 * escopo organizacional da requisição.
 */
@Schema(description = "DTO contendo todas as métricas consolidadas do dashboard")
public record DashboardMetricsDTO(
        @Schema(description = "Resumo geral numérico")
        ResumoGeralDTO resumoGeral,

        @Schema(description = "Distribuição de eventos por organização no período")
        List<DistribuicaoEventosDTO> eventosPorGrupoParoquial,

        @Schema(description = "Distribuição de projetos por faixa de status")
        ProjetosPorStatusDTO projetosPorStatus,

        @Schema(description = "Detalhes de execução e progresso dos projetos ativos")
        List<DetalheProjetoDTO> projetosAtivosDetalhes,

        @Schema(description = "Eventos sem projeto vinculado no período, com sugestão de vínculo")
        List<EventoOrfaoDTO> eventosOrfaos,

        @Schema(description = "Eventos agendados para a semana corrente")
        List<EventoSemanalDTO> eventosSemana
) {
}
