package br.com.nsfatima.gestao.projeto.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resumo agregado de um projeto, combinando status de execução dos eventos, mapa de
 * colaboração entre organizações e indicadores de saúde temporal (ver
 * {@code ProjetoAgregacaoDomainService}).
 */
@Schema(description = "Resumo agregado do projeto")
public record ProjetoResumoDTO(
        StatusExecucaoDTO statusExecucao,
        MapaColaboracaoDTO mapaColaboracao,
        SaudeTemporalDTO saudeTemporal
) {
}
