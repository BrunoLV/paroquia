package br.com.nsfatima.gestao.calendario.domain.exception;

public class ApprovalRequiredException extends RuntimeException {

    public ApprovalRequiredException(String message) {
        super(message);
    }
}
