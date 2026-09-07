package br.com.nsfatima.gestao.fiel.api.v1.dto;

/** Identificação civil mascarada no transporte. */
public record IdentificacaoDto(String cpfFormatado, String rg, String paisOrigem,
        String tipoDocumentoEstrangeiro, String documentoEstrangeiroFormatado) { }
