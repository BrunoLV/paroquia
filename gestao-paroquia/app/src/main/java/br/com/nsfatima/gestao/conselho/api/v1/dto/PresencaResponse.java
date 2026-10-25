package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.SituacaoPresenca;

import java.util.UUID;

/** Presença nominal com o nome resolvido na leitura. */
public record PresencaResponse(UUID fielId, String nome, String funcao, SituacaoPresenca situacao) {
}
