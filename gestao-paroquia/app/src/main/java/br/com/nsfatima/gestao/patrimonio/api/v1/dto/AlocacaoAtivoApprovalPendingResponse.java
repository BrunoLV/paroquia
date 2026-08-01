package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import java.util.UUID;

public record AlocacaoAtivoApprovalPendingResponse(
        UUID solicitacaoAprovacaoId,
        String status,
        UUID ativoId,
        UUID eventoId,
        String mensagem) {
}
