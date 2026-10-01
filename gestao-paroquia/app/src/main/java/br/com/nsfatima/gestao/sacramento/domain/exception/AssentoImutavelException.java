package br.com.nsfatima.gestao.sacramento.domain.exception;

/** Transição ou edição vedada pelo estado atual do assento (selado, cancelado ou ainda rascunho). */
public class AssentoImutavelException extends RuntimeException {

    public AssentoImutavelException(String message) {
        super(message);
    }
}
