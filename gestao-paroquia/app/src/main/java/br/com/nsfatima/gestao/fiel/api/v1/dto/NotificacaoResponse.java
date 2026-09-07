package br.com.nsfatima.gestao.fiel.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

/** Resposta de notificação para o usuário. */
public record NotificacaoResponse(
        UUID id,
        String tipo,
        String mensagem,
        Instant criadaEmUtc,
        Instant lidaEmUtc) {
}
