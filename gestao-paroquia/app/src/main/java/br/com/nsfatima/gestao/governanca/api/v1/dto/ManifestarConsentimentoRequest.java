package br.com.nsfatima.gestao.governanca.api.v1.dto;

/** Payload enviado pelo titular para conceder ou atualizar suas preferências de consentimento. */
public record ManifestarConsentimentoRequest(
        boolean termoAceito,
        boolean usoImagemAutorizado,
        boolean permitirDivulgacaoAniversario,
        String responsavelMenorNome,
        String responsavelMenorCpf
) {}
