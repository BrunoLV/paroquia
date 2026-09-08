package br.com.nsfatima.gestao.reserva.domain.exception;

public class ReservaNaoAutorizadaException extends RuntimeException {
    public ReservaNaoAutorizadaException(String message) {
        super(message);
    }
}
