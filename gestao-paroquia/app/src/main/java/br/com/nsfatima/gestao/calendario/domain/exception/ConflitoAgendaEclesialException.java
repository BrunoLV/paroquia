package br.com.nsfatima.gestao.calendario.domain.exception;

public class ConflitoAgendaEclesialException extends RuntimeException {
    public ConflitoAgendaEclesialException() {
        super("Conflito de Agenda Eclesial");
    }

    public ConflitoAgendaEclesialException(String message) {
        super(message);
    }
}
