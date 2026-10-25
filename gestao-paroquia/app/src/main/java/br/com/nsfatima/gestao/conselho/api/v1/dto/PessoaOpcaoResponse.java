package br.com.nsfatima.gestao.conselho.api.v1.dto;

import java.util.UUID;

/** Fiel encontrado pela busca da presença: identificador e nome. */
public record PessoaOpcaoResponse(UUID id, String nome) {
}
