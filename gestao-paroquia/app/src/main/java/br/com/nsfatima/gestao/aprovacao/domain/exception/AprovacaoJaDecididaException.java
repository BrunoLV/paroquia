package br.com.nsfatima.gestao.aprovacao.domain.exception;

public class AprovacaoJaDecididaException extends RuntimeException {

    public AprovacaoJaDecididaException(String message) {
        super(message);
    }
}
