package br.com.nsfatima.gestao.governanca.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

/** Dados consolidados devolvidos após validação do código OTP para preenchimento da esteira. */
public record ValidarCodigoConsentimentoResponse(
        String token,
        String titularTipo,
        UUID titularId,
        String nome,
        boolean isMenor,
        boolean termoAceito,
        String termoVersao,
        String termoHash,
        String textoTermo,
        boolean usoImagemAutorizado,
        boolean permitirDivulgacaoAniversario,
        String responsavelMenorNome,
        String responsavelMenorCpf,
        Instant dataHoraUltimaManifestacaoUtc
) {}
