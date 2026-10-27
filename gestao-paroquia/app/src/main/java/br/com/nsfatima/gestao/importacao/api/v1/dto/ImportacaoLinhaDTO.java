package br.com.nsfatima.gestao.importacao.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Linha aprovada na validação e reenviada para confirmação. Carrega o número da linha no arquivo original para
 * que um erro na gravação seja apontado na mesma posição que o usuário vê na planilha.
 *
 * @param linha número da linha no arquivo original (cabeçalho = linha 1)
 * @param dados valores da linha por nome de coluna; são revalidados integralmente na confirmação
 */
public record ImportacaoLinhaDTO(
        @Schema(description = "Número da linha no arquivo original (cabeçalho = linha 1)")
        @Positive int linha,
        @Schema(description = "Valores da linha por nome de coluna")
        @NotNull @Size(max = 30) Map<String, String> dados
) {
}
