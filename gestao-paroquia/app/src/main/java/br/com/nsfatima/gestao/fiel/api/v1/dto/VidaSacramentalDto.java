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
        String certidaoMatrimonio,
        String nomePaiCertidao,
        String nomeMaeCertidao,
        String testemunhasMatrimonio
) {
    /** Mantém compatibilidade binária e de testes com o contrato anterior. */
    public VidaSacramentalDto(
            Boolean batizado, LocalDate dataBatismo, String paroquiaBatismo, String celebranteBatismo,
            String livroBatismo, String folhaBatismo, String termoBatismo, String padrinhosBatismo,
            Boolean primeiraComunhao, LocalDate dataPrimeiraComunhao, String paroquiaPrimeiraComunhao,
            String turmaCatequese, Boolean crismado, LocalDate dataCrisma, String paroquiaCrisma,
            String celebranteCrisma, String padrinhoCrisma, Boolean casadoIgrejaCatolica,
            LocalDate dataMatrimonio, String paroquiaMatrimonio, String numeroProcessoMatrimonial,
            String certidaoMatrimonio) {
        this(batizado, dataBatismo, paroquiaBatismo, celebranteBatismo, livroBatismo, folhaBatismo,
                termoBatismo, padrinhosBatismo, primeiraComunhao, dataPrimeiraComunhao,
                paroquiaPrimeiraComunhao, turmaCatequese, crismado, dataCrisma, paroquiaCrisma,
                celebranteCrisma, padrinhoCrisma, casadoIgrejaCatolica, dataMatrimonio,
                paroquiaMatrimonio, numeroProcessoMatrimonial, certidaoMatrimonio, null, null, null);
    }
}
