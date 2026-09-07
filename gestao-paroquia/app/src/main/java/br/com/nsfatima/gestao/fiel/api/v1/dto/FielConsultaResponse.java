package br.com.nsfatima.gestao.fiel.api.v1.dto;

/** Contrato de leitura de fiel, variando conforme a permissão de quem consulta. */
public sealed interface FielConsultaResponse permits FielResponse, FielContatoResponse {
}
