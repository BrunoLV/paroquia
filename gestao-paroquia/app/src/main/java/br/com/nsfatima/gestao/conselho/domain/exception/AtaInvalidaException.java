package br.com.nsfatima.gestao.conselho.domain.exception;

/** Conteúdo da ata viola uma invariante estrutural ou está incompleto para a etapa que se tenta cumprir. */
public class AtaInvalidaException extends RuntimeException {

    public AtaInvalidaException(String message) {
        super(message);
    }
}
