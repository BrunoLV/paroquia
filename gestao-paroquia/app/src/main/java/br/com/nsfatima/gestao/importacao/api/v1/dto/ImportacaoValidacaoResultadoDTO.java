package br.com.nsfatima.gestao.importacao.api.v1.dto;

import java.util.List;

/**
 * Sumário da etapa de pré-validação de um arquivo de importação em lote (etapa "dry-run" antes
 * da confirmação): totais agregados por linha válida/inválida e o detalhe item a item, usado
 * pela UI para exibir uma prévia antes do usuário confirmar a persistência.
 *
 * @param totalLinhas quantidade total de linhas de dados lidas do arquivo (exclui o cabeçalho)
 * @param totalValidos quantidade de linhas aptas a serem persistidas em {@code confirmarImportacao}
 * @param totalInvalidos quantidade de linhas rejeitadas por alguma regra de validação
 * @param itens resultado individual de cada linha, na ordem em que aparecem no arquivo
 */
public record ImportacaoValidacaoResultadoDTO(
        int totalLinhas,
        int totalValidos,
        int totalInvalidos,
        List<ImportacaoItemResultadoDTO> itens
) {
}
