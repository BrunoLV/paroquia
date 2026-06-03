package br.com.nsfatima.gestao.aprovacao.domain.exception;

public class ApprovalAlreadyDecidedException extends RuntimeException {

    public ApprovalAlreadyDecidedException(String message) {
        super(message);
    }
}
