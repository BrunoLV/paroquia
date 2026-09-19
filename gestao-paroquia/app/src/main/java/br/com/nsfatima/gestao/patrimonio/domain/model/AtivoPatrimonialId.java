package br.com.nsfatima.gestao.patrimonio.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Identificador fortemente tipado do Agregado {@link AtivoPatrimonial}. Garante imutabilidade e
 * validação estrutural do UUID já na camada de domínio, evitando o uso de {@code UUID} cru como
 * chave e o acoplamento acidental entre agregados por referência direta.
 */
public record AtivoPatrimonialId(UUID value) implements Serializable {

    public AtivoPatrimonialId {
        Objects.requireNonNull(value, "O identificador do ativo patrimonial não pode ser nulo.");
    }

    public static AtivoPatrimonialId gerar() {
        return new AtivoPatrimonialId(UUID.randomUUID());
    }

    public static AtivoPatrimonialId fromString(String raw) {
        Objects.requireNonNull(raw, "A string do identificador do ativo patrimonial não pode ser nula.");
        return new AtivoPatrimonialId(UUID.fromString(raw.trim()));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
