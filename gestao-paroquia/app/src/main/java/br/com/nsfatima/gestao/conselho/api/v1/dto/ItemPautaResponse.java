package br.com.nsfatima.gestao.conselho.api.v1.dto;

import java.util.List;

/** Item da ordem do dia com as deliberações dele decorrentes. */
public record ItemPautaResponse(int ordem, String titulo, String relato, List<DeliberacaoResponse> deliberacoes) {
}
