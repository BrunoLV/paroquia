package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import jakarta.validation.constraints.Size;

/**
 * Solicitação de desalocação (devolução antecipada) de um ativo alocado a um evento,
 * com observação opcional registrando o motivo da devolução.
 */
public record DesalocarAtivoRequest(
        @Size(max = 4000) String observacao
) {
}
