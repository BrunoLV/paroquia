package br.com.nsfatima.gestao.projeto.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Representação de uma solicitação de aprovação pendente (ou já decidida) sobre um projeto —
 * de criação ({@code tipoSolicitacao = "CRIACAO"}) ou cancelamento
 * ({@code tipoSolicitacao = "CANCELAMENTO"}). Retornada quando uma ação sobre projeto é
 * desviada para aprovação por {@code TransicionarProjetoUseCase}.
 */
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
