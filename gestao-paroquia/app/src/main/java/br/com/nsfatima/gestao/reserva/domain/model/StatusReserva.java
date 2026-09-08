package br.com.nsfatima.gestao.reserva.domain.model;

import java.util.Locale;

/**
 * Estados do ciclo de vida operacional de uma Reserva.
 */
public enum StatusReserva {
    PENDENTE("PENDENTE", "Pendente de Aprovação"),
    CONFIRMADA("CONFIRMADA", "Confirmada"),
    RETIRADO("RETIRADO", "Retirado / Em Custódia"),
    FINALIZADA("FINALIZADA", "Finalizada"),
    CANCELADA("CANCELADA", "Cancelada"),
    EXPIRADA("EXPIRADA", "Expirada por Limite de Tempo");

    private final String codigo;
    private final String descricao;

    StatusReserva(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean ocupaAgenda() {
        return this != CANCELADA && this != EXPIRADA;
    }

    public static StatusReserva fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("O status da reserva não pode ser nulo ou vazio.");
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (StatusReserva status : values()) {
            if (status.name().equals(normalized) || status.codigo.equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status de reserva inválido: " + raw);
    }
}
