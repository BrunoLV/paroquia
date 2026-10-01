package br.com.nsfatima.gestao.sacramento.domain.exception;

/** Assento inexistente ou invisível ao ator (livro reservado nunca revela a própria existência). */
public class AssentoNaoEncontradoException extends RuntimeException {

    public AssentoNaoEncontradoException(String message) {
        super(message);
    }
}
