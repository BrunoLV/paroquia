package br.com.nsfatima.gestao.governanca.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

/** Comprovante oficial emitido após concessão ou revogação de consentimento. */
public record ComprovanteConsentimentoResponse(
        UUID protocolo,
        String titularNome,
        String tipoEvento,
        Instant dataHoraUtc,
        String termoVersao,
        String termoHash,
        String mensagem
) {}
