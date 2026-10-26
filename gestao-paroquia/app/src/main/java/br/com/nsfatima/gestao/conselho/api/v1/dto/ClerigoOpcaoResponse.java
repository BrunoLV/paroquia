package br.com.nsfatima.gestao.conselho.api.v1.dto;

import java.util.UUID;

/** Clérigo que pode constar na presença da ata, com a função que o encargo sugere. */
public record ClerigoOpcaoResponse(UUID id, String nome, String funcao, boolean paroco) {
}
