package br.com.nsfatima.gestao.importacao.api.v1.dto;

import java.util.List;

/**
 * Resultado da confirmação (RN-IMP-01): o erro de uma linha é isolado e relatado, sem impedir a gravação das
 * demais.
 *
 * @param registrosImportados linhas efetivamente gravadas
 * @param enviadosParaAprovacao linhas que viraram solicitação de aprovação e ainda não foram gravadas
 * @param erros linhas rejeitadas, na ordem do arquivo, com o motivo
 */
public record ImportacaoConfirmacaoResultadoDTO(
        int registrosImportados,
        int enviadosParaAprovacao,
        List<ImportacaoErroLinhaDTO> erros
) {
}
