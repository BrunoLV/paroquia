package br.com.nsfatima.gestao.fiel.api.v1.dto;

import java.time.Instant;

public record ConsentimentoLgpdDto(
        Boolean termoConsentimentoAceito,
        Instant dataHoraConsentimentoUtc,
        String ipConsentimento,
        String versaoTermoConsentimento,
        Boolean usoImagemAutorizado,
        Boolean permitirDivulgacaoAniversario
) {
}
