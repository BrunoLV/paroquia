package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import jakarta.validation.constraints.Size;

public record DesalocarAtivoRequest(
        @Size(max = 4000) String observacao
) {
}
