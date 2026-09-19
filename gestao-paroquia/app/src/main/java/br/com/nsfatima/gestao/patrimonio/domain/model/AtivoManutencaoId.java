package br.com.nsfatima.gestao.patrimonio.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Identificador fortemente tipado do Agregado {@link AtivoManutencao}. Uma manutenção é um
 * agregado próprio (ciclo de vida e invariantes independentes do ativo), associado ao ativo
 * apenas por {@link AtivoPatrimonialId}.
 */
public record AtivoManutencaoId(UUID value) implements Serializable {

    public AtivoManutencaoId {
        Objects.requireNonNull(value, "O identificador da manutenção não pode ser nulo.");
    }

    public static AtivoManutencaoId gerar() {
        return new AtivoManutencaoId(UUID.randomUUID());
    }

    public static AtivoManutencaoId fromString(String raw) {
        Objects.requireNonNull(raw, "A string do identificador da manutenção não pode ser nula.");
        return new AtivoManutencaoId(UUID.fromString(raw.trim()));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
