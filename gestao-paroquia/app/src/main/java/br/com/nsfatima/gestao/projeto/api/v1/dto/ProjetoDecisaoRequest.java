package br.com.nsfatima.gestao.projeto.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Payload para decidir (aprovar/reprovar) uma solicitação de aprovação pendente de um projeto
 * (criação ou cancelamento), usado por {@code TransicionarProjetoUseCase#decidir}. Só pode ser
 * enviado por um ator de alto nível.
 */
@Schema(description = "Solicitação de decisão sobre aprovação de projeto")
public record ProjetoDecisaoRequest(
        @Schema(description = "Se a solicitação foi aprovada ou não")
        boolean aprovado,

        @Schema(description = "Observação sobre a decisão")
        @Size(max = 2000) String observacao
) {
}
