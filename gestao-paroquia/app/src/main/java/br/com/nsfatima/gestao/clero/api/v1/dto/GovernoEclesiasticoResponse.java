package br.com.nsfatima.gestao.clero.api.v1.dto;

import java.time.LocalDate;
import java.util.UUID;

public record GovernoEclesiasticoResponse(
        UUID id,
        UUID dioceseId,
        String dioceseNome,
        String bispoDiocesano,
        UUID parocoId,
        String parocoNome,
        String parocoNomeEclesiastico,
        LocalDate dataPosseParoco,
        String decretoErecaoParoquia,
        LocalDate dataInstalacaoParoquia,
        String livroTomboNumero
) {
}
