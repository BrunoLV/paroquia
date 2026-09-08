package br.com.nsfatima.gestao.reserva.domain.exception;

import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;

import java.util.UUID;

public class ReservaNotFoundException extends RuntimeException {
    public ReservaNotFoundException(ReservaId id) {
        super("Reserva não encontrada para o identificador: " + id.value());
    }

    public ReservaNotFoundException(UUID id) {
        super("Reserva não encontrada para o identificador: " + id);
    }
}
