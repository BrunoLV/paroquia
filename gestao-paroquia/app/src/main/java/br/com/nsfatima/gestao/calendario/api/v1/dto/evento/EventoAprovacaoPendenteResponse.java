package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import java.util.UUID;

public record EventoAprovacaoPendenteResponse(
        UUID solicitacaoAprovacaoId,
        String status,
        String mensagem) {
}
