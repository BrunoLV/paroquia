package br.com.nsfatima.gestao.fiel.api.v1.dto;

import java.util.UUID;

/** Projeção mínima para escolher um fiel: identificador e nome, sem nenhum dado de contato ou documento. */
public record FielNomeResponse(UUID fielId, String nome) {
}
