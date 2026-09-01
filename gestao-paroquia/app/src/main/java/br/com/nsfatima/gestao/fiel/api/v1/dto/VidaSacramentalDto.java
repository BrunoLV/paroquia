package br.com.nsfatima.gestao.fiel.api.v1.dto;

import java.time.LocalDate;

public record VidaSacramentalDto(
        Boolean batizado,
        LocalDate dataBatismo,
        String paroquiaBatismo,
        String celebranteBatismo,
        String livroBatismo,
        String folhaBatismo,
        String termoBatismo,
        String padrinhosBatismo,

        Boolean primeiraComunhao,
        LocalDate dataPrimeiraComunhao,
        String paroquiaPrimeiraComunhao,
        String turmaCatequese,

        Boolean crismado,
        LocalDate dataCrisma,
        String paroquiaCrisma,
        String celebranteCrisma,
        String padrinhoCrisma,

        Boolean casadoIgrejaCatolica,
        LocalDate dataMatrimonio,
        String paroquiaMatrimonio,
        String numeroProcessoMatrimonial,
        String certidaoMatrimonio
) {
}
