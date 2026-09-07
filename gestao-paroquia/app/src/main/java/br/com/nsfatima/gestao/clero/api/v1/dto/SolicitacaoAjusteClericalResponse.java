package br.com.nsfatima.gestao.clero.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

/** Visão moderada da solicitação para a Secretaria e a administração. */
public record SolicitacaoAjusteClericalResponse(
        UUID id,
        UUID clerigoId,
        String status,
        String email,
        String celular,
        String telefoneFixo,
        String endereco,
        Instant criadoEmUtc) {
}
