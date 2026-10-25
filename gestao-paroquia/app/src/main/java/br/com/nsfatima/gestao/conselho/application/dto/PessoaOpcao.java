package br.com.nsfatima.gestao.conselho.application.dto;

import java.util.UUID;

/** Fiel que o redator pode escolher para a presença de uma reunião de presença livre: só identificador e nome. */
public record PessoaOpcao(UUID id, String nome) {
}
