package br.com.nsfatima.gestao.conselho.domain.exception;

/**
 * Edição vedada porque a ata já foi selada (homologada ou registrada). A correção de uma ata selada
 * é feita exclusivamente por ata retificadora (RN-CON-03).
 */
public class AtaImutavelException extends RuntimeException {

    public AtaImutavelException(String message) {
        super(message);
    }
}
