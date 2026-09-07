package br.com.nsfatima.gestao.fiel.api.v1.dto;

import java.util.UUID;

/** Projeção mínima autorizada para coordenadores de pastoral. */
public record FielContatoResponse(UUID fielId, String nome, ContatoDto contato)
        implements FielConsultaResponse {
}
