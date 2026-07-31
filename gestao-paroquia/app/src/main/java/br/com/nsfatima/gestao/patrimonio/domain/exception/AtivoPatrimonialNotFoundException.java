package br.com.nsfatima.gestao.patrimonio.domain.exception;

public class AtivoPatrimonialNotFoundException extends RuntimeException {

    public AtivoPatrimonialNotFoundException(String message) {
        super(message);
    }
}
