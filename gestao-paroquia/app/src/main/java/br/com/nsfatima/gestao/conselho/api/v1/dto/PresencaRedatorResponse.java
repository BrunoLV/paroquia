package br.com.nsfatima.gestao.conselho.api.v1.dto;

import java.util.UUID;

/** Fiel que redige a ata e a função com que deve constar na presença. */
public record PresencaRedatorResponse(UUID fielId, String funcao) {
}
