package br.com.nsfatima.gestao.conselho.application.dto;

/**
 * Dois layouts do mesmo registro: {@code ESTRUTURADA} apresenta seções e tabelas; {@code NARRATIVA} gera o
 * texto corrido de Livro de Atas a partir dos mesmos campos, sem redigitação.
 */
public enum LayoutExportacao {
    ESTRUTURADA,
    NARRATIVA
}
