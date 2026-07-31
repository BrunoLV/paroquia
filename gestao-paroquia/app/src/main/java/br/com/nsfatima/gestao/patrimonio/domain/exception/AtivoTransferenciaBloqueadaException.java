package br.com.nsfatima.gestao.patrimonio.domain.exception;

public class AtivoTransferenciaBloqueadaException extends RuntimeException {

    public AtivoTransferenciaBloqueadaException(String message) {
        super(message);
    }
}
