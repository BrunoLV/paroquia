package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import java.util.UUID;

public record ApprovalDecisionEvent(
        UUID aprovacaoId,
        UUID eventoId,
        String solicitanteId,
        AprovacaoStatus status,
        String decisorId,
        String erroExecucao) {
}
