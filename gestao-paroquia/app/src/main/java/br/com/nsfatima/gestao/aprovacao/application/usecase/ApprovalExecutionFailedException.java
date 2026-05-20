package br.com.nsfatima.gestao.aprovacao.application.usecase;

public class ApprovalExecutionFailedException extends RuntimeException {

    public ApprovalExecutionFailedException(String message) {
        super(message);
    }
}
