package br.com.nsfatima.gestao.reserva.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Identificador fortemente tipado do Agregado Reserva.
 * Garante imutabilidade e validação estrutural do UUID na camada de domínio.
 */
public record ReservaId(UUID value) implements Serializable {

    public ReservaId {
        Objects.requireNonNull(value, "O identificador da reserva não pode ser nulo.");
    }

    public static ReservaId gerar() {
        return new ReservaId(UUID.randomUUID());
    }

    public static ReservaId fromString(String raw) {
        Objects.requireNonNull(raw, "A string do identificador da reserva não pode ser nula.");
        return new ReservaId(UUID.fromString(raw.trim()));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
