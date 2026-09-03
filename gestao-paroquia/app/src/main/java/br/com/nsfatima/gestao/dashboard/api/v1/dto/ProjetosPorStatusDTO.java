package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Distribuição dos projetos paroquiais pelas quatro faixas exibidas no dashboard.
 * {@code atrasados} não é um status persistido: é derivado no cálculo (projeto ativo com prazo
 * estourado ou em risco), então um mesmo projeto conta simultaneamente em {@code emExecucao} e
 * {@code atrasados}.
 *
 * @param planejamento projetos em status {@code ABERTO}
 * @param emExecucao projetos em status {@code EM_ANDAMENTO}
 * @param concluidos projetos em status {@code CONCLUIDO}
 * @param atrasados projetos ativos com prazo vencido ou classificados em risco
 */
@Schema(description = "Distribuição de projetos por faixa de status no dashboard")
public record ProjetosPorStatusDTO(
        int planejamento,
        int emExecucao,
        int concluidos,
        int atrasados
) {
}
