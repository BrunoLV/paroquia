package br.com.nsfatima.gestao.governanca.domain.exception;

/** Indica que o ator não possui autoridade canônica para alterar a governança anual. */
public class AnoParoquialNaoAutorizadoException extends RuntimeException {

    public AnoParoquialNaoAutorizadoException(String mensagem) {
        super(mensagem);
    }
}
