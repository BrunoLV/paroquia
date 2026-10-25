package br.com.nsfatima.gestao.calendario.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Payload de requisição para atualização dos dados do Encarregado de Proteção de Dados (DPO).
 */
@Schema(description = "Requisição para atualização dos dados do Encarregado de Proteção de Dados (DPO)")
public record AtualizarDpoRequest(
        @Schema(description = "Nome formal do Encarregado de Dados (DPO)", example = "Dr. Carlos Eduardo")
        @Size(max = 150, message = "Nome do DPO deve ter no máximo 150 caracteres.")
        String dpoNome,

        @Schema(description = "E-mail institucional oficial do DPO", example = "dpo@nsfatima.com.br")
        @Email(message = "E-mail do DPO deve ser um endereço de e-mail válido.")
        @Size(max = 150, message = "E-mail do DPO deve ter no máximo 150 caracteres.")
        String dpoEmail,

        @Schema(description = "Telefone institucional de contato do DPO", example = "(11) 2031-0001")
        @Size(max = 20, message = "Telefone do DPO deve ter no máximo 20 caracteres.")
        String dpoTelefone
) {
}
