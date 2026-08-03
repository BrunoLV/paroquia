package br.com.nsfatima.gestao.importacao.api.v1.dto;

import java.util.Map;

/**
 * Resultado da validação de uma única linha do arquivo importado. {@code linha} é 1-based e já
 * inclui o deslocamento do cabeçalho (linha 2 do arquivo = primeira linha de dados), para casar
 * com o que o usuário vê ao abrir a planilha/CSV original.
 *
 * @param linha número da linha no arquivo original (cabeçalho = linha 1)
 * @param valido {@code true} se a linha passou em todas as regras de validação do tipo
 * @param mensagemErro motivo da rejeição, ou {@code null} quando {@code valido} é {@code true}
 * @param dados valores brutos da linha, indexados por nome de coluna, ecoados de volta para
 *     permitir correção/reenvio pelo usuário
 */
public record ImportacaoItemResultadoDTO(
        int linha,
        boolean valido,
        String mensagemErro,
        Map<String, String> dados
) {
}
