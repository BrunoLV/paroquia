package br.com.nsfatima.gestao.conselho.domain.model;

/**
 * Apuração do quórum de uma reunião. É informativa: o documento exportado declara se o quórum foi atingido,
 * mas a ata não deixa de ser lavrada por isso.
 */
public record ResultadoQuorum(int membrosConvocados, int minimoNecessario, int presentes, boolean atingido) {
}
