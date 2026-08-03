package br.com.nsfatima.gestao.aprovacao.domain.event;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;

import java.util.UUID;

public record AprovacaoDecisaoEvent(
        UUID aprovacaoId,
        UUID eventoId,
        String solicitanteId,
        AprovacaoStatus status,
        String decisorId,
        String erroExecucao) {
}
