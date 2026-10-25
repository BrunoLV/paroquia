package br.com.nsfatima.gestao.governanca.api.v1.dto;

/** Resposta da solicitação de código com e-mail mascarado para segurança. */
public record SolicitarCodigoConsentimentoResponse(
        String emailMascarado,
        int expiraEmSegundos,
        String mensagem
) {}
