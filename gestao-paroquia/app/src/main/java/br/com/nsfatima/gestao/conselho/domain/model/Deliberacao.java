package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;

import java.util.List;
import java.util.Objects;

/**
 * Deliberação tomada sobre um item de pauta. É o registro durável de "o que o colegiado entendeu", sem
 * ciclo de vida próprio: vive e se sela junto com a {@link AtaReuniao}. {@code votacao} é opcional e só é
 * aceita nos tipos de reunião que votam.
 */
public record Deliberacao(
        int ordem,
        String texto,
        ResultadoDeliberacao resultado,
        Votacao votacao,
        List<ConflitoInteresse> conflitos) {

    private static final int TEXTO_MAX = 4000;

    public Deliberacao {
        if (ordem < 1) {
            throw new AtaInvalidaException("A ordem da deliberação começa em 1.");
        }
        if (texto == null || texto.isBlank()) {
            throw new AtaInvalidaException("O texto da deliberação é obrigatório.");
        }
        texto = texto.trim();
        if (texto.length() > TEXTO_MAX) {
            throw new AtaInvalidaException("O texto da deliberação excede " + TEXTO_MAX + " caracteres.");
        }
        Objects.requireNonNull(resultado, "O resultado da deliberação é obrigatório.");
        conflitos = conflitos == null ? List.of() : List.copyOf(conflitos);
    }
}
