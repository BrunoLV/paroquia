package br.com.nsfatima.gestao.sacramento.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Identificador fortemente tipado do agregado {@link AssentoSacramental}. */
public record AssentoSacramentalId(UUID value) implements Serializable {

    public AssentoSacramentalId {
        Objects.requireNonNull(value, "O identificador do assento sacramental não pode ser nulo.");
    }

    public static AssentoSacramentalId gerar() {
        return new AssentoSacramentalId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
