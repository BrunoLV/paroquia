package br.com.nsfatima.gestao.sacramento.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Retificação material atestada pelo Pároco; acrescenta-se ao termo, nunca o sobrescreve. */
public record Ressalva(
        UUID id, String campo, String valorOriginal, String valorCorrigido, String justificativa,
        String atestadaPor, Instant atestadaEmUtc) {

    public Ressalva {
        if (campo == null || campo.isBlank() || valorCorrigido == null || valorCorrigido.isBlank()) {
            throw new IllegalArgumentException("Campo e valor corrigido da ressalva são obrigatórios.");
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("Justificativa da retificação é obrigatória.");
        }
    }
}
