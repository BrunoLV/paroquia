package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitação de cancelamento de uma alocação de ativo ainda não iniciada/efetivada,
 * exigindo motivo para preservar rastreabilidade do cancelamento.
 */
public record CancelarAlocacaoRequest(
        @NotBlank @Size(min = 10, max = 4000) String motivoCancelamento
) {
}
