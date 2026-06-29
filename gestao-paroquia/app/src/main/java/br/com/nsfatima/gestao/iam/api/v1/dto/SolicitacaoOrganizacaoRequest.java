package br.com.nsfatima.gestao.iam.api.v1.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SolicitacaoOrganizacaoRequest(
    @NotNull(message = "Id da organização é obrigatório")
    UUID organizacaoId,
    
    UUID igrejaVinculoId
) {}
