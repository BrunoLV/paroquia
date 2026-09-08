package br.com.nsfatima.gestao.reserva.domain.model;

import java.util.Locale;

/**
 * Define a natureza do recurso sujeito a reserva na paróquia.
 */
public enum TipoItemReserva {
    LOCAL("LOCAL", "Dependência Física"),
    ATIVO("ATIVO", "Bem Móvel / Ativo Patrimonial");

    private final String codigo;
    private final String descricao;

    TipoItemReserva(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public static TipoItemReserva fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("O tipo de item da reserva não pode ser nulo ou vazio.");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (TipoItemReserva tipo : values()) {
            if (tipo.name().equals(normalized) || tipo.codigo.equals(normalized)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de item de reserva inválido: " + raw);
    }
}
