package br.com.nsfatima.gestao.fiel.domain.model;

import java.time.LocalDate;

/**
 * Objeto de Valor (Value Object) contendo o registro da Vida Sacramental do Fiel
 * (Batismo, Primeira Comunhão, Crisma e Matrimônio com referências aos livros paroquiais).
 */
public record VidaSacramental(
        boolean batizado,
        LocalDate dataBatismo,
        String paroquiaBatismo,
        String celebranteBatismo,
        String livroBatismo,
        String folhaBatismo,
        String termoBatismo,
        String padrinhosBatismo,

        boolean primeiraComunhao,
        LocalDate dataPrimeiraComunhao,
        String paroquiaPrimeiraComunhao,
        String turmaCatequese,

        boolean crismado,
        LocalDate dataCrisma,
        String paroquiaCrisma,
        String celebranteCrisma,
        String padrinhoCrisma,

        boolean casadoIgrejaCatolica,
        LocalDate dataMatrimonio,
        String paroquiaMatrimonio,
        String numeroProcessoMatrimonial,
        String certidaoMatrimonio
) {
}
