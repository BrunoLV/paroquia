package br.com.nsfatima.gestao.projeto.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjetoAprovacaoResponse(
        UUID id,
        UUID projetoId,
        String tipoSolicitacao,
        String status,
        String solicitanteId,
        Instant criadoEmUtc,
        Instant decididoEmUtc,
        String actionPayloadJson,
        Boolean cancelarEventosVinculados
) {
}
