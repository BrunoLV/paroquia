package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Corpo da requisição para substituir a lista completa de organizações envolvidas em um evento.
 * A lista informada é tratada como o novo estado completo dos envolvidos, não como um incremento —
 * organizações previamente envolvidas e ausentes desta lista são removidas do vínculo.
 */
public record EventoEnvolvidosRequest(
        @NotNull List<EventoEnvolvidoInput> envolvidos) {
}
