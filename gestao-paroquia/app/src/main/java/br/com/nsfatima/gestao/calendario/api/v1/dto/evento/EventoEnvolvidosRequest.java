package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record EventoEnvolvidosRequest(
        @NotNull List<EventoEnvolvidoInput> envolvidos) {
}
