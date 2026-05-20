package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import java.util.UUID;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import jakarta.validation.constraints.NotNull;

public record AddMembershipRequest(
    @NotNull UUID organizacaoId,
    @NotNull TipoOrganizacao tipo,
    @NotNull PapelOrganizacional papel
) {}
