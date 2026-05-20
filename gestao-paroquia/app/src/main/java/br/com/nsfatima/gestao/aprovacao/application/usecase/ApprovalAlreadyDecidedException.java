package br.com.nsfatima.gestao.aprovacao.application.usecase;

public class ApprovalAlreadyDecidedException extends RuntimeException {

    public ApprovalAlreadyDecidedException(String message) {
        super(message);
    }
}
