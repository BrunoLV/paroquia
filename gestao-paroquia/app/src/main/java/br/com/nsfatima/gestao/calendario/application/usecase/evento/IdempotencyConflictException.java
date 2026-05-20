package br.com.nsfatima.gestao.calendario.application.usecase.evento;

public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String message) {
        super(message);
    }
}
