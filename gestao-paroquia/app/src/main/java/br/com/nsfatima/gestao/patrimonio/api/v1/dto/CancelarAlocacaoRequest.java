package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarAlocacaoRequest(
        @NotBlank @Size(min = 10, max = 4000) String motivoCancelamento
) {
}
