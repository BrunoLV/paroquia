package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Indicadores numéricos de topo do dashboard, já filtrados pelo escopo organizacional do ator
 * quando aplicável. Reúne volume de eventos e fila de aprovações (recorte de calendário) com a
 * base cadastral de fiéis e o engajamento pastoral (recorte de comunidade), para dar em um único
 * bloco a leitura de "quanta gente" e "quanta atividade".
 *
 * @param totalEventosPeriodo eventos com início dentro do recorte temporal selecionado
 * @param projetosAtivos projetos em status {@code ABERTO} ou {@code EM_ANDAMENTO}
 * @param solicitacoesPendentes aprovações pendentes (eventos + projetos)
 * @param totalFieisAtivos fiéis com cadastro ativo
 * @param novosFieisMes fiéis cadastrados desde o primeiro dia do mês corrente (sempre mensal)
 * @param fieisEngajadosPastoral fiéis distintos com vínculo ativo a uma pastoral ou movimento
 * @param fieisSemVinculoPastoral fiéis ativos sem nenhum vínculo pastoral ativo
 */
@Schema(description = "Resumo geral numérico do dashboard")
public record ResumoGeralDTO(
        int totalEventosPeriodo,
        int projetosAtivos,
        int solicitacoesPendentes,
        long totalFieisAtivos,
        long novosFieisMes,
        long fieisEngajadosPastoral,
        long fieisSemVinculoPastoral
) {
}
