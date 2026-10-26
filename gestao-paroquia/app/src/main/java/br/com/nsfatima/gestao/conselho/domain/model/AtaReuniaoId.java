package br.com.nsfatima.gestao.conselho.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Identificador fortemente tipado do agregado {@link AtaReuniao}. Evita o uso de {@code UUID} cru como
 * chave e impede que outro agregado referencie a ata por tipo errado.
 */
public record AtaReuniaoId(UUID value) implements Serializable {

    public AtaReuniaoId {
        Objects.requireNonNull(value, "O identificador da ata não pode ser nulo.");
    }

    public static AtaReuniaoId gerar() {
        return new AtaReuniaoId(UUID.randomUUID());
    }

    public static AtaReuniaoId fromString(String raw) {
        Objects.requireNonNull(raw, "A string do identificador da ata não pode ser nula.");
        return new AtaReuniaoId(UUID.fromString(raw.trim()));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
