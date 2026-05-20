package br.com.nsfatima.gestao.aprovacao.application.usecase;

public class ApprovalNotFoundException extends RuntimeException {

    public ApprovalNotFoundException(String message) {
        super(message);
    }
}
