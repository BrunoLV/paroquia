package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import java.util.UUID;

public record VinculoResponse(
        UUID id,
        UUID organizacaoId,
        String organizacaoNome,
        String tipoOrganizacao,
        String papel,
        boolean ativo,
        UUID igrejaVinculoId,
        String igrejaVinculoNome
) {
}
