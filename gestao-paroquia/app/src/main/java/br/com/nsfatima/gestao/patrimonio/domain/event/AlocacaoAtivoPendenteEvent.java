package br.com.nsfatima.gestao.patrimonio.domain.event;

import java.util.UUID;

public record AlocacaoAtivoPendenteEvent(
        UUID aprovacaoId,
        UUID ativoId,
        UUID eventoId,
        String solicitanteId) {
}
