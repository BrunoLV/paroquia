package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HomologarCpcDecisaoRequest(
        @NotNull StatusHomologacao status,
        @Size(max = 2000) String motivoVeto
) {
}
