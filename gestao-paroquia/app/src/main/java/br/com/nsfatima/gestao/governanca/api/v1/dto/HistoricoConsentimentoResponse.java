package br.com.nsfatima.gestao.governanca.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

/** Registro individual na trilha imutável de auditoria de consentimento. */
public record HistoricoConsentimentoResponse(
        UUID id,
        String tipoEvento,
        String canal,
        String metodoAutenticacao,
        String termoVersao,
        String termoHash,
        boolean termoAceito,
        boolean usoImagemAutorizado,
        boolean permitirDivulgacaoAniversario,
        String responsavelMenorNome,
        String responsavelMenorCpf,
        String ipOrigem,
        String userAgent,
        Instant criadoEmUtc
) {}
