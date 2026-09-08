package br.com.nsfatima.gestao.reserva.domain.exception;

public class ReservaOperacaoInvalidaException extends RuntimeException {
    public ReservaOperacaoInvalidaException(String message) {
        super(message);
    }
}
