package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarEventoRequest(
        @NotBlank @Size(max = 2000) String motivo) {
}
