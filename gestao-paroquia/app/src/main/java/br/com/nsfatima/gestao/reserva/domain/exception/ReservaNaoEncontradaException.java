package br.com.nsfatima.gestao.reserva.domain.exception;

import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;

import java.util.UUID;

public class ReservaNaoEncontradaException extends RuntimeException {
    public ReservaNaoEncontradaException(ReservaId id) {
        super("Reserva não encontrada para o identificador: " + id.value());
    }

    public ReservaNaoEncontradaException(UUID id) {
        super("Reserva não encontrada para o identificador: " + id);
    }
}
