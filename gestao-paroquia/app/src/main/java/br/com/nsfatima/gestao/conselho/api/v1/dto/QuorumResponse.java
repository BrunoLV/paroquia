package br.com.nsfatima.gestao.conselho.api.v1.dto;

/** Quórum apurado: maioria absoluta dos membros convocados, congelada na submissão. */
public record QuorumResponse(int membrosConvocados, int minimoNecessario, int presentes, boolean atingido) {
}
