package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AdicionarVinculoRequest(
        @NotNull UUID organizacaoId,
        @NotNull TipoOrganizacao tipo,
        @NotNull PapelOrganizacional papel,
        UUID igrejaVinculoId
) {
    public UUID communityId() {
        return igrejaVinculoId;
    }
}
