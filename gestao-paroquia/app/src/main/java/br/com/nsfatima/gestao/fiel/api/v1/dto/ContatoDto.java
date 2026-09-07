package br.com.nsfatima.gestao.fiel.api.v1.dto;

/** Canais de contato formatados para apresentação. */
public record ContatoDto(String celularFormatado, boolean isWhatsapp, String telefoneFixoFormatado,
        String email) { }
