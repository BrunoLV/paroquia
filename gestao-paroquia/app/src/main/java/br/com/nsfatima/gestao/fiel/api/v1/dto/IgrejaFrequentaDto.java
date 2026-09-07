package br.com.nsfatima.gestao.fiel.api.v1.dto;

import java.util.UUID;

/** Unidade territorial em que o fiel participa da vida comunitária. */
public record IgrejaFrequentaDto(UUID id, String nome, String tipo) { }
