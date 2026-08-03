package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import java.util.UUID;

public record AlocacaoAtivoAprovacaoPendenteResponse(
        UUID solicitacaoAprovacaoId,
        String status,
        UUID ativoId,
        UUID eventoId,
        String mensagem) {
}
