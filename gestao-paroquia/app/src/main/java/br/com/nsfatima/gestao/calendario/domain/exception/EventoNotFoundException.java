package br.com.nsfatima.gestao.calendario.domain.exception;

import java.util.UUID;

public class EventoNotFoundException extends RuntimeException {

    public EventoNotFoundException(UUID eventoId) {
        super("Evento not found: " + eventoId);
    }
}
