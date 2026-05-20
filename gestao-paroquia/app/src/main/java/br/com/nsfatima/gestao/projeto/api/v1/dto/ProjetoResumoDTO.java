package br.com.nsfatima.gestao.projeto.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo agregado do projeto")
public record ProjetoResumoDTO(
        StatusExecucaoDTO statusExecucao,
        MapaColaboracaoDTO mapaColaboracao,
        SaudeTemporalDTO saudeTemporal
) {
}
