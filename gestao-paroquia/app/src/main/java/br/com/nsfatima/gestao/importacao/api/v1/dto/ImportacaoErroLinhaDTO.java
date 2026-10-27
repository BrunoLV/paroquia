package br.com.nsfatima.gestao.importacao.api.v1.dto;

/**
 * Motivo pelo qual uma linha não foi gravada na confirmação. A mensagem é devolvida só a quem enviou o arquivo
 * e nunca vai para log, pois pode citar um identificador informado na própria linha.
 *
 * @param linha número da linha no arquivo original
 * @param mensagem regra violada, em linguagem de negócio
 */
public record ImportacaoErroLinhaDTO(int linha, String mensagem) {
}
