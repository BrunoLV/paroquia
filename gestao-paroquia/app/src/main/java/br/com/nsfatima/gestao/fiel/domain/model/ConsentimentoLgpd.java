package br.com.nsfatima.gestao.fiel.domain.model;

import java.time.Instant;

/**
 * Objeto de Valor registrando a manifestação de consentimento LGPD e preferências de privacidade.
 */
public record ConsentimentoLgpd(
        boolean termoConsentimentoAceito,
        Instant dataHoraConsentimentoUtc,
        String ipConsentimento,
        String versaoTermoConsentimento,
        boolean usoImagemAutorizado,
        boolean permitirDivulgacaoAniversario
) {
}
