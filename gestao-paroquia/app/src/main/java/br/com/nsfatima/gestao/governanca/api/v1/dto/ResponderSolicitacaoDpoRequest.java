package br.com.nsfatima.gestao.governanca.api.v1.dto;

import br.com.nsfatima.gestao.governanca.domain.model.StatusSolicitacaoDpo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Payload utilizado pelo DPO para responder formalmente ao pedido do titular. */
public record ResponderSolicitacaoDpoRequest(
        @NotBlank(message = "A resposta ao titular é obrigatória.")
        String resposta,

        @NotNull(message = "O status final da solicitação é obrigatório.")
        StatusSolicitacaoDpo status
) {}
