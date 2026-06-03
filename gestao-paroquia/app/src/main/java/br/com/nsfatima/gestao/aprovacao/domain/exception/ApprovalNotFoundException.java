package br.com.nsfatima.gestao.aprovacao.domain.exception;

public class ApprovalNotFoundException extends RuntimeException {

    public ApprovalNotFoundException(String message) {
        super(message);
    }
}
